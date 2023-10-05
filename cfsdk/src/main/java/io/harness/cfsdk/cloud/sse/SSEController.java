package io.harness.cfsdk.cloud.sse;

import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.OkSse;
import io.harness.cfsdk.cloud.oksse.SSEListener;
import io.harness.cfsdk.cloud.oksse.ServerSentEvent;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.common.SdkCodes;
import io.harness.cfsdk.utils.TlsUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SSEController implements SSEControlling {

    private final AuthInfo authInfo;
    private ServerSentEvent serverSentEvent;

    public SSEController(AuthInfo authInfo) {

        this.authInfo = authInfo;
    }

    @Override
    public synchronized void start(SSEConfig config, EventsListener eventsListener, boolean isRescheduled) {

        if (config != null && config.getAuthentication() != null) {

            final String cluster = authInfo.getCluster();

            Request request = new Request.Builder()
                    .url(config.getUrl() + "?cluster=" + cluster)
                    .build();

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true);

            if (config.getCfConfig() != null) {
                TlsUtils.setupTls(clientBuilder, config.getCfConfig().getTlsTrustedCAs());
            }

            OkSse okSse = new OkSse(clientBuilder.build());

            serverSentEvent = okSse.newServerSentEvent(

                    request,
                    new SSEListener(eventsListener),
                    config.getAuthentication(),
                    isRescheduled,
                    authInfo
            );
        }
    }

    @Override
    public synchronized void stop() {

        if (serverSentEvent == null) {

            return;
        }
        serverSentEvent.close();
        SdkCodes.infoStreamStopped();
    }


}
