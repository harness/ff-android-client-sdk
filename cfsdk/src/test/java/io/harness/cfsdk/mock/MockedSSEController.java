package io.harness.cfsdk.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.cloud.sse.SSEControlling;

public class MockedSSEController implements SSEControlling {

    private static final Logger log = LoggerFactory.getLogger(MockedSSEController.class);
    private EventsListener listener;



    @Override
    public void start(SSEConfig config, EventsListener eventsListener, boolean isRescheduled) {

        log.debug("Start");

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

        log.debug("Stop");
        listener = null;
    }

    public EventsListener getListener() {

        return listener;
    }
}
