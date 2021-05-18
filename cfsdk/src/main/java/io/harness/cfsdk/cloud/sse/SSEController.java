package io.harness.cfsdk.cloud.sse;

import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.OkSse;
import io.harness.cfsdk.cloud.oksse.SSEListener;
import io.harness.cfsdk.cloud.oksse.ServerSentEvent;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import okhttp3.Request;

public class SSEController {
    private ServerSentEvent serverSentEvent;

    public synchronized void start(SSEConfig config, EventsListener eventsListener) {
        if (config != null && config.getAuthentication() != null) {
            Request request =
                    new Request.Builder()
                            .url(config.getUrl())
                            .build();
            OkSse okSse = new OkSse();
            serverSentEvent = okSse.newServerSentEvent(request, new SSEListener(eventsListener),
                    config.getAuthentication());
        }
    }

    public synchronized void stop() {
        if (serverSentEvent == null) return;
        serverSentEvent.close();
//        if (sseStatusListener != null) sseStatusListener.onClose(serverSentEvent);
//        serverSentEvent = null;
    }
}
