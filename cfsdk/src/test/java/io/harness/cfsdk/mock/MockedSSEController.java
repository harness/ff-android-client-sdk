package io.harness.cfsdk.mock;

import com.google.common.cache.Cache;

import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.cloud.sse.SSEControlling;
import io.harness.cfsdk.logging.CfLog;

public class MockedSSEController implements SSEControlling {

    private EventsListener listener;

    private final String logTag;

    {

        logTag = MockedSSEController.class.getSimpleName();
    }

    public MockedSSEController(Cache<String, FeatureConfig> featureCache) {

        featureCache.put(MockedFeatureRepository.MOCK_BOOL, new FeatureConfig());
        featureCache.put(MockedFeatureRepository.MOCK_NUMBER, new FeatureConfig());
        featureCache.put(MockedFeatureRepository.MOCK_STRING, new FeatureConfig());
    }

    @Override
    public void start(SSEConfig config, EventsListener eventsListener) {

        CfLog.OUT.v(logTag, "Start");

        listener = eventsListener;
        listener.onEventReceived(

                new StatusEvent(

                        StatusEvent.EVENT_TYPE.SSE_START,
                        null
                )
        );
    }

    @Override
    public void stop() {

        CfLog.OUT.v(logTag, "Stop");
        listener = null;
    }

    public EventsListener getListener() {

        return listener;
    }
}
