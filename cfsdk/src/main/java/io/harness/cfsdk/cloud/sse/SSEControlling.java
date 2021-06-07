package io.harness.cfsdk.cloud.sse;

import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;

public interface SSEControlling {

    void start(SSEConfig config, EventsListener eventsListener);

    void stop();
}
