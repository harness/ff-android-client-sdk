package io.harness.cfsdk.cloud.sse;

import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.OkSse;
import io.harness.cfsdk.cloud.oksse.SSEListener;
import io.harness.cfsdk.cloud.oksse.ServerSentEvent;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import okhttp3.Request;

public class SSEController implements SSEControlling {


    private final AuthInfo authInfo;
    private ServerSentEvent serverSentEvent;

    public SSEController(AuthInfo authInfo) {

        this.authInfo = authInfo;
    }

    @Override
    public synchronized void start(SSEConfig config, EventsListener eventsListener) {

        if (config != null && config.getAuthentication() != null) {

            final String cluster = authInfo.getCluster();

            Request request = new Request.Builder()
                    .url(config.getUrl() + "?cluster=" + cluster)
                    .build();

            OkSse okSse = new OkSse();

            serverSentEvent = okSse.newServerSentEvent(

                    request,
                    new SSEListener(eventsListener),
                    config.getAuthentication()
            );
        }
    }

    @Override
    public synchronized void stop() {

        if (serverSentEvent == null) {

            return;
        }
        serverSentEvent.close();
    }
}
