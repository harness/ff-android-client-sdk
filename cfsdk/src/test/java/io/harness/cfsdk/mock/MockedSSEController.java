package io.harness.cfsdk.mock;

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
