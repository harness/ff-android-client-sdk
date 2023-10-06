package io.harness.cfsdk.cloud.sse;


import io.harness.cfsdk.cloud.sse.StatusEvent;

public interface EventsListener {
    void onEventReceived(StatusEvent statusEvent);
}
