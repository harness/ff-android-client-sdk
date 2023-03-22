/*
 *  Copyright (c) 2016 HERE Europe B.V.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.harness.cfsdk.cloud.oksse;

import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.logging.CfLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

class RealServerSentEvent implements ServerSentEvent {

    private Call call;
    public Listener listener;
    private Reader sseReader;
    private String lastEventId;
    private long reconnectTime = TimeUnit.SECONDS.toMillis(3);
    private final String logTag = RealServerSentEvent.class.getSimpleName();
    private OkHttpClient client;
    private long readTimeoutMillis;
    private final Request originalRequest;
    private final SSEAuthentication authentication;
    private final AuthInfo authInfo;

    RealServerSentEvent(Request request, Listener listener, SSEAuthentication sseAuthentication, AuthInfo authInfo) {
        this.authentication = sseAuthentication;
        if (!"GET".equals(request.method())) {
            throw new IllegalArgumentException("Request must be GET: " + request.method());
        }
        this.originalRequest = request;
        this.listener = listener;
        this.authInfo = authInfo;
    }

    void connect(OkHttpClient client, boolean isRescheduled) {
        this.client = client;
        prepareCall(originalRequest);
        enqueue(isRescheduled);
    }

    private void prepareCall(Request request) {
        if (client == null) {
            throw new AssertionError("Client is null");
        }
        Request.Builder requestBuilder = request.newBuilder()
                .header("Accept-Encoding", "")
                .header("Accept", "text/event-stream")
                .header("Cache-Control", "no-cache")
                .header("API-Key", this.authentication.getApiToken())
                .header("Authorization", "Bearer " + this.authentication.getAuthToken())
                .header("User-Agent", "android " + ANDROID_SDK_VERSION)
                .header("Harness-SDK-Info", "Android " + ANDROID_SDK_VERSION + " Client")
                .header("Harness-EnvironmentID", authInfo.getEnvironment())
                .header("Harness-AccountID", authInfo.getAccountID());

        if (lastEventId != null) {
            requestBuilder.header("Last-Event-Id", lastEventId);
        }

        call = client.newCall(requestBuilder.build());
    }

    private void enqueue(boolean isRescheduled) {

        CfLog.OUT.v(logTag, "API, SSE starting");
        call.enqueue(
                new Callback() {

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        CfLog.OUT.e(logTag, "API, SSE failure", e);
                        notifyFailure(e, null, isRescheduled);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {

                        CfLog.OUT.v(

                                logTag,
                                String.format("API, SSE Response received: %s", response.code())
                        );
                        if (response.isSuccessful()) {

                            openSse(response, isRescheduled);
                        } else {

                            notifyFailure(new IOException(response.message()), response, isRescheduled);
                        }
                    }
                }
        );
    }

    private void openSse(Response response, boolean isRescheduled) {

        try (ResponseBody body = response.body()) {

            sseReader = new Reader(response);
            sseReader.setTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
            if (listener != null) listener.onOpen(this, response, isRescheduled);

            //noinspection StatementWithEmptyBody
            while (call != null && !call.isCanceled() && sseReader.read(isRescheduled)) {
            }
        }
    }

    private void notifyFailure(Throwable throwable, Response response, boolean isRescheduled) {

        CfLog.OUT.e(logTag, "Error in opening SSE stream", throwable);
        if (!retry(throwable, response, isRescheduled)) {

            if (listener != null) listener.onClosed(this);
            close();
        }
    }

    private void notifyClosed() {
        CfLog.OUT.e(logTag, "End of SSE stream encountered");
        close();
        if (listener != null) listener.onClosed(this);
    }

    private boolean retry(Throwable throwable, Response response, boolean isRescheduled) {
        if (!Thread.currentThread().isInterrupted() && !call.isCanceled() && listener != null && listener.onRetryError(this, throwable, response)) {
            Request request = listener.onPreRetry(this, originalRequest);
            if (request == null) {
                return false;
            }
            prepareCall(request);
            try {
                Thread.sleep(reconnectTime);
            } catch (InterruptedException ignored) {
                return false;
            }
            if (!Thread.currentThread().isInterrupted() && !call.isCanceled()) {
                enqueue(isRescheduled);
                return true;
            }
        }
        return false;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public void setTimeout(long timeout, TimeUnit unit) {
        if (sseReader != null) {
            sseReader.setTimeout(timeout, unit);
        }
        readTimeoutMillis = unit.toMillis(timeout);
    }

    @Override
    public void close() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    /**
     * Internal reader for the SSE channel. This will wait for data being send and will parse it according to the
     * SSE standard.
     *
     * @see Reader#read(boolean isRescheduled)
     */
    private class Reader {

        private static final char COLON_DIVIDER = ':';
        private static final String UTF8_BOM = "\uFEFF";

        private static final String DATA = "data";
        private static final String ID = "id";
        private static final String EVENT = "event";
        private static final String RETRY = "retry";
        private static final String DEFAULT_EVENT = "message";
        private static final String EMPTY_STRING = "";

        private final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]+$");

        private final ResponseBody body;
        private final Response response;
        private final BufferedSource source;

        // Intentionally done to reuse StringBuilder for memory optimization
        @SuppressWarnings("PMD.AvoidStringBufferField")
        private StringBuilder data = new StringBuilder();
        private String eventName = DEFAULT_EVENT;

        Reader(Response response) {
            this.response = response;
            this.body = response.body();
            this.source = body.source();
        }

        /**
         * Blocking call that will try to read a line from the source
         *
         * @return true if the read was successfully, false if an error was thrown
         */
        boolean read(boolean isRescheduled) {
            try {
                String line = source.readUtf8Line();
                if (line == null) {
                    notifyClosed();
                    return false;
                }
                processLine(line);
            } catch (IOException e) {

                CfLog.OUT.e(logTag, e.getMessage(), e);
                notifyFailure(e, null, isRescheduled);
                return false;
            }
            return true;
        }

        /**
         * Sets a reading timeout, so the read operation will get unblock if this timeout is reached.
         *
         * @param timeout timeout to set
         * @param unit    unit of the timeout to set
         */
        void setTimeout(long timeout, TimeUnit unit) {
            if (source != null) {
                source.timeout().timeout(timeout, unit);
            }
        }

        private void processLine(String line) {
            //log("Sse read line: " + line);
            if (line == null || line.isEmpty()) { // If the line is empty (a blank line). Dispatch the event.
                dispatchEvent();
                return;
            }

            int colonIndex = line.indexOf(COLON_DIVIDER);
            if (colonIndex == 0 && listener != null) { // If line starts with COLON dispatch a comment
                listener.onComment(RealServerSentEvent.this, line.substring(1).trim());
            } else if (colonIndex != -1) { // Collect the characters on the line after the first U+003A COLON character (:), and let value be that string.
                String field = line.substring(0, colonIndex);
                String value = EMPTY_STRING;
                int valueIndex = colonIndex + 1;
                if (valueIndex < line.length()) {
                    if (line.charAt(valueIndex) == ' ') { // If value starts with a single U+0020 SPACE character, remove it from value.
                        valueIndex++;
                    }
                    value = line.substring(valueIndex);
                }
                processField(field, value);
            } else {
                processField(line, EMPTY_STRING);
            }
        }

        private void dispatchEvent() {
            if (data.length() == 0) {
                return;
            }
            String dataString = data.toString();
            if (dataString.endsWith("\n")) {
                dataString = dataString.substring(0, dataString.length() - 1);
            }
            if (listener != null)
                listener.onMessage(RealServerSentEvent.this, lastEventId, eventName, dataString);
            data.setLength(0);
            eventName = DEFAULT_EVENT;
        }

        private void processField(String field, String value) {
            if (DATA.equals(field)) {
                data.append(value).append('\n');
            } else if (ID.equals(field)) {
                lastEventId = value;
            } else if (EVENT.equals(field)) {
                eventName = value;
            } else if (RETRY.equals(field) && DIGITS_ONLY.matcher(value).matches()) {
                long timeout = Long.parseLong(value);
                if (listener != null && listener.onRetryTime(RealServerSentEvent.this, timeout)) {
                    reconnectTime = timeout;
                }
            }
        }
    }
}
