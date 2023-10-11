package io.harness.cfsdk.cloud.sse;

public interface EventsListener {
    void onEventReceived(StatusEvent statusEvent);
}
