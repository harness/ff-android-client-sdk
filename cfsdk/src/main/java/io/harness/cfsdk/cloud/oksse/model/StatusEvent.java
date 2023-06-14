package io.harness.cfsdk.cloud.oksse.model;


import io.harness.cfsdk.cloud.core.model.Evaluation;

/**
 * Base class describing events that can be triggered via {@link io.harness.cfsdk.cloud.oksse.EventsListener}.
 * This class is simple, with type described in {@link EVENT_TYPE} and generic payload.
 * Use {@link #extractPayload()} to get encapsulated data.
 * <strong>Be very careful when working with payload of this class.</strong> It's type agnostic, meaning it will
 * try to convert encapsulated data into one the caller is asking for.
 *
 * For possible types check documentation covered in {@link EVENT_TYPE}.
 *
 */
public class StatusEvent {

    public enum EVENT_TYPE {
        /**
         * Realtime evaluation update is started. Has no payload.
         */
        SSE_START,
        /**
         * Realtime evaluation update has restarted after recovering from network failure.
         * Will reload latest evaluations from server. Has no payload.
         */
        SSE_RESUME,
        /**
         * Realtime evaluation update is stopped. Has no payload.
         */
        SSE_END,
        /**
         * Evaluation is changed via realtime evaluation update. Will not be called if stream is disabled.
         * The payload is of type {@link Evaluation}
         *
         * @see io.harness.cfsdk.cloud.core.client.Configuration
         */
        EVALUATION_CHANGE,
        /**
         * Evaluation is removed via realtime evaluation update. The payload is of type {@link Evaluation}
         */
        EVALUATION_REMOVE,
        /**
         * Evaluations have been reloaded via polling mechanism. The payload is list of loaded evaluations; it's not
         * change-sensitive, i.e. it will be triggered even if new evaluations are same as already stored ones.
         */
        EVALUATION_RELOAD
    }

    private final EVENT_TYPE eventType;
    private final Object payload;

    public StatusEvent(EVENT_TYPE eventType, Object payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public EVENT_TYPE getEventType() {
        return eventType;
    }

    public <T> T extractPayload() {
        return (T) payload;
    }
}
