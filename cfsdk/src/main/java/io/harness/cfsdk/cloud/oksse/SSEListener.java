package io.harness.cfsdk.cloud.oksse;


import com.google.common.cache.Cache;

import org.json.JSONException;
import org.json.JSONObject;

import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.logging.CfLog;
import okhttp3.Request;
import okhttp3.Response;

public class SSEListener implements ServerSentEvent.Listener {

    private final Cloud cloud;
    private final String logTag;
    private final AuthInfo authInfo;
    private final EventsListener eventsListener;
    private final Cache<String, FeatureConfig> featureCache;

    {

        logTag = SSEListener.class.getSimpleName();
    }

    public SSEListener(

            Cloud cloud,
            AuthInfo authInfo,
            EventsListener eventsListener,
            Cache<String, FeatureConfig> featureCache
    ) {

        this.cloud = cloud;
        this.authInfo = authInfo;
        this.eventsListener = eventsListener;
        this.featureCache = featureCache;
    }

    @Override
    public void onOpen(ServerSentEvent serverSentEvent, Response response) {
        if (this.eventsListener != null) {

            this.eventsListener.onEventReceived(
                    new StatusEvent(StatusEvent.EVENT_TYPE.SSE_START, serverSentEvent)
            );
        }
    }

    @Override
    public void onMessage(ServerSentEvent serverSentEvent, String id, String event, String message) {

        JSONObject jsonObject;
        try {

            jsonObject = new JSONObject(message);
            String identifier = jsonObject.getString("identifier");
            String eventType = jsonObject.getString("event");
            String domain = jsonObject.getString("domain");

            Evaluation evaluation = new Evaluation();
            evaluation.flag(identifier);

            if ("create".equals(eventType) || "patch".equals(eventType)) {

                eventsListener.onEventReceived(

                        new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, evaluation)
                );
            } else if ("delete".equals(eventType)) {

                eventsListener.onEventReceived(

                        new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, evaluation)
                );
            }

            if ("flag".equals(domain)) {

                processFeature(jsonObject);
            }
        } catch (JSONException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
    }

    @Override
    public void onComment(ServerSentEvent serverSentEvent, String s) {

    }

    @Override
    public boolean onRetryTime(ServerSentEvent serverSentEvent, long l) {
        return false;
    }

    @Override
    public boolean onRetryError(
            ServerSentEvent serverSentEvent, Throwable throwable, Response response) {
        return false;
    }

    @Override
    public void onClosed(ServerSentEvent serverSentEvent) {
        if (this.eventsListener != null)
            this.eventsListener.onEventReceived(new StatusEvent(StatusEvent.EVENT_TYPE.SSE_END, serverSentEvent));
    }

    @Override
    public Request onPreRetry(ServerSentEvent serverSentEvent, Request request) {
        return null;
    }

    private void processFeature(JSONObject jsonObject) {

        CfLog.OUT.v(logTag, "Syncing the latest features..");
        try {

            final String identifier = jsonObject.getString("identifier");
            final Long version = jsonObject.getLong("version");

            for (int i = 0; i < 3; i++) {
                try {

                    final String environmentID = authInfo.getEnvironmentIdentifier();
                    final String clusterID = authInfo.getClusterIdentifier();

                    FeatureConfig featureConfig =
                            cloud.getFeatureConfigByIdentifier(

                                    identifier,
                                    environmentID,
                                    clusterID
                            );

                    if (version.equals(featureConfig.getVersion())) {

                        featureCache.put(featureConfig.getFeature(), featureConfig);
                        break;
                    }
                } catch (ApiException e) {

                    CfLog.OUT.e(

                            logTag,
                            String.format(

                                    "Failed to sync the feature %s due to %s",
                                    identifier,
                                    e.getMessage()
                            ),
                            e
                    );
                }
            }
        } catch (JSONException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
    }
}
