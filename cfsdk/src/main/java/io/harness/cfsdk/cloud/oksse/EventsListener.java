package io.harness.cfsdk.cloud.oksse;

import io.harness.cfsdk.cloud.oksse.model.StatusEvent;

public interface EventsListener {
    void onEventReceived(StatusEvent statusEvent);
}
