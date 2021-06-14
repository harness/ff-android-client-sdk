package io.harness.cfsdk.cloud.sse;

import com.google.common.cache.Cache;

import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.OkSse;
import io.harness.cfsdk.cloud.oksse.SSEListener;
import io.harness.cfsdk.cloud.oksse.ServerSentEvent;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import okhttp3.Request;

public class SSEController implements SSEControlling {

    private final ICloud cloud;
    private final AuthInfo authInfo;
    private ServerSentEvent serverSentEvent;
    private final Cache<String, FeatureConfig> featureCache;

    public SSEController(

            ICloud cloud,
            AuthInfo authInfo,
            Cache<String, FeatureConfig> featureCache
    ) {

        this.cloud = cloud;
        this.authInfo = authInfo;
        this.featureCache = featureCache;
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
                    new SSEListener(cloud, authInfo, eventsListener, featureCache),
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
//        if (sseStatusListener != null) sseStatusListener.onClose(serverSentEvent);
//        serverSentEvent = null;
    }
}
