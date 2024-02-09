package io.harness.cfsdk.cloud.sse;

import static io.harness.cfsdk.cloud.sse.StatusEvent.makeSseEndEvent;
import static io.harness.cfsdk.cloud.sse.StatusEvent.makeSseResumeEvent;
import static io.harness.cfsdk.cloud.sse.StatusEvent.makeSseStartEvent;
import static java.util.concurrent.ThreadLocalRandom.current;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;

import io.harness.cfsdk.AndroidSdkVersion;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;
import io.harness.cfsdk.cloud.network.NewRetryInterceptor;
import io.harness.cfsdk.common.SdkCodes;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSource implements Callback, AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(EventSource.class);
  private final EventsListener eventListener;
  private final HttpLoggingInterceptor loggingInterceptor;
  private final long retryBackoffDelay;
  private final String url;
  private final Map<String, String> headers;
  private final long sseReadTimeoutMins;
  private final List<X509Certificate> trustedCAs;
  private final CfConfiguration config;
  private OkHttpClient streamClient;
  private Call call;

  public EventSource(
          @NonNull String url,
          Map<String, String> headers,
          @NonNull EventsListener eventListener,
          long sseReadTimeoutMins,
          CfConfiguration config) {
    this.url = url;
    this.headers = headers;
    this.eventListener = eventListener;
    this.sseReadTimeoutMins = sseReadTimeoutMins;
    this.retryBackoffDelay = current().nextInt(2000, 5000);
    this.trustedCAs = config.getTlsTrustedCAs();
    this.loggingInterceptor = new HttpLoggingInterceptor();
    this.config = config;
  }

  protected OkHttpClient makeStreamClient(long sseReadTimeoutMins, List<X509Certificate> trustedCAs) {
    OkHttpClient.Builder httpClientBuilder =
        new OkHttpClient.Builder()
            .eventListener(EventListener.NONE)
            .readTimeout(sseReadTimeoutMins, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true);

    setupTls(httpClientBuilder, trustedCAs);

    if (log.isDebugEnabled()) {
      httpClientBuilder.addInterceptor(loggingInterceptor);
    } else {
      httpClientBuilder.interceptors().remove(loggingInterceptor);
    }

    httpClientBuilder.addInterceptor(new NewRetryInterceptor(retryBackoffDelay));
    return httpClientBuilder.build();
  }

  private void setupTls(OkHttpClient.Builder httpClientBuilder, List<X509Certificate> trustedCAs) {

    try {
      if (trustedCAs != null && !trustedCAs.isEmpty()) {

        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        for (int i = 0; i < trustedCAs.size(); i++) {
          keyStore.setCertificateEntry("ca" + i, trustedCAs.get(i));
        }

        final TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());

        httpClientBuilder.sslSocketFactory(
            sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
      }
    } catch (GeneralSecurityException | IOException ex) {
      String msg = "Failed to setup TLS on SSE endpoint: " + ex.getMessage();
      logExceptionAndWarn(msg, ex);
      throw new RuntimeException(msg, ex);
    }
  }

  public void start(boolean isRescheduled) {
    log.info("EventSource connecting with url {}", url);
    if (log.isDebugEnabled()) {
      log.debug("EventSource headers {}", redactHeaders(headers));
    }

    this.streamClient = makeStreamClient(sseReadTimeoutMins, trustedCAs);

    final Request.Builder builder =
        new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Android " + AndroidSdkVersion.ANDROID_SDK_VERSION)
            .addHeader("X-Request-ID", UUID.randomUUID().toString());


    for (Map.Entry<String, String> next: headers.entrySet()) {
      builder.addHeader(next.getKey(), next.getValue());
    }


    // SSE can be started from two entrypoints in CFClient:
    // 1. initialize
    // 2. reschedule
    // So make sure the right event is sent depending on how SSE has been started.
    if (isRescheduled) {
      eventListener.onEventReceived(makeSseResumeEvent());
    }
    eventListener.onEventReceived(makeSseStartEvent());

    SdkCodes.infoStreamConnected();

    this.call = streamClient.newCall(builder.build());

    call.enqueue(this);
  }


  public void stop() {
    log.info("Stopping EventSource service.");

    if (call != null) {
      call.cancel();
    }
  }

  public void close() {
    stop();
    this.streamClient.connectionPool().evictAll();
    log.info("EventSource closed");
  }

  @Override // Callback
  public void onFailure(@NotNull Call call, @NotNull IOException e) {
    SdkCodes.warnStreamDisconnected(e.getMessage());
    logExceptionAndWarn("SSE stream error", e);
    eventListener.onEventReceived(makeSseEndEvent());
  }

  @Override // Callback
  public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
    log.debug("SSE stream data: {}", response.message());

    try {
      if (!response.isSuccessful()) {
        throw new SSEStreamException("Invalid SSE HTTP response: " + response);
      }

      if (response.body() == null) {
        throw new SSEStreamException("Invalid SSE HTTP response: empty body");
      }

      final BufferedSource reader = response.body().source();

      String line;
      while ((line = reader.readUtf8Line()) != null) {
        log.debug("SSE stream data: {}", line);

        if (line.startsWith("data:")) {
          SdkCodes.infoStreamEventReceived(line.substring(6));
          sseMessage(line.substring(6));
        }
      }

      throw new SSEStreamException("End of SSE stream");
    } catch (Throwable ex) {
      SdkCodes.warnStreamDisconnected(ex.getMessage());
      logExceptionAndWarn("SSE stream aborted", ex);
      eventListener.onEventReceived(makeSseEndEvent());
    }
  }

  private static class SSEStreamException extends RuntimeException {
    public SSEStreamException(String msg, Throwable cause) {
      super(msg, cause);
    }

    public SSEStreamException(String msg) {
      super(msg);
    }
  }

  private void sseMessage(String message) {

    try {
      final JSONObject jsonObject = new JSONObject(message);
      final String domain = jsonObject.getString("domain");
      final String eventType = jsonObject.getString("event");
      final String identifier = jsonObject.getString("identifier");

      final Type evaluationsType = new TypeToken<ArrayList<Evaluation>>(){}.getType();
      ArrayList<Evaluation> evaluations = new ArrayList<>();
      evaluations.add(new Evaluation());
      evaluations.get(0).flag(identifier);

      // parse the actual evaluations sent in the sse event
      try {
        String evaluationJSON = jsonObject.getString("evaluations");
        Gson gson= new Gson();
        evaluations = gson.fromJson(evaluationJSON,evaluationsType);
      } catch (Exception e) {
        // this will happen if the evaluations aren't sent down the stream with the event
        // it's not an error case so no need to log it
      }

      log.debug("onMessage(): domain={}, eventType={}, identifier={}", domain, eventType, identifier);

      StatusEvent statusEvent = null;
      if ("target-segment".equals(domain)) {
        // On creation, change or removal of a target group we want to reload evaluations
        if ("delete".equals(eventType) || "patch".equals(eventType) || "create".equals(eventType)) {
          statusEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluations);
        }
      }

      if ("flag".equals(domain)) {
        // On creation or change of a flag we want to send a change event
        if ("create".equals(eventType) || "patch".equals(eventType)) {
          statusEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, evaluations);
        }
        // On deletion of a flag we want to send a remove event
        if ("delete".equals(eventType)) {
          statusEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, evaluations.get(0));
        }
      }

      if (statusEvent != null) {
        eventListener.onEventReceived(statusEvent);
      } else {
        log.debug("Unrecognized Status Event received, Ignoring... onMessage(): domain={}, eventType={}, identifier={}", domain, eventType, identifier);
      }

    } catch (JSONException e) {
      logExceptionAndWarn(e.getMessage(), e);
    }
  }

  void logExceptionAndWarn(String msg, Throwable ex) {
    log.warn(msg);
    if (config.isDebugEnabled()) {
      log.warn(msg + " STACKTRACE", ex);
    }
  }

  private Map<String, String> redactHeaders(Map<String, String> map) {
    final Map<String, String> cloned = new HashMap<>(map);
    cloned.put("Authorization", "*");
    cloned.put("API-Key", "*");
    return cloned;
  }
}
