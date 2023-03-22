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

import io.harness.cfsdk.cloud.model.AuthInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class OkSse {

    private final OkHttpClient client;

    /**
     * Creates a new OkSse using the shared {@link OkHttpClient}
     *
     * @param client Client instance.
     */
    public OkSse(OkHttpClient client) {
        this.client = client;
    }

    /**
     * Get the {@link OkHttpClient} used to create this instance.
     *
     * @return the instance of the {@link OkHttpClient}
     */
    public OkHttpClient getClient() {
        return client;
    }

    /**
     * Create a new instance of {@link ServerSentEvent} that will handle the connection and communication with
     * the SSE Server.
     *
     * @param request        the OkHttp {@link Request} with the valid information to create the connection with the server.
     * @param listener       the {@link io.harness.cfsdk.cloud.oksse.ServerSentEvent.Listener} to attach to this SSE.
     * @param authentication Auth. parameters.
     * @return a new instance of {@link ServerSentEvent} that will automatically start the connection.
     */
    public ServerSentEvent newServerSentEvent(

            Request request,
            ServerSentEvent.Listener listener,
            SSEAuthentication authentication,
            boolean isRescheduled,
            AuthInfo authInfo
    ) {

        RealServerSentEvent sse = new RealServerSentEvent(request, listener, authentication, authInfo);
        sse.connect(client, isRescheduled);
        return sse;
    }
}
