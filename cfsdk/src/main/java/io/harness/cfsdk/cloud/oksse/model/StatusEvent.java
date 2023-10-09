package io.harness.cfsdk.cloud.oksse.model;

/**
 * @deprecated
 * Since 1.2.0, use {@link io.harness.cfsdk.cloud.sse.StatusEvent} instead.
 * Will be removed in a future release
 */
@Deprecated
public class StatusEvent extends io.harness.cfsdk.cloud.sse.StatusEvent {
    public StatusEvent(EVENT_TYPE eventType) {
        super(eventType);
    }
}
