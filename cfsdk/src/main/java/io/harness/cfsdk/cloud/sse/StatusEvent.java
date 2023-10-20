package io.harness.cfsdk.cloud.sse;


import java.util.List;

import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

/**
 * Base class describing events that can be triggered via {@link EventsListener}.
 * This class is simple, with type described in {@link EVENT_TYPE} and generic payload.
 * Use {@link #extractEvaluationListPayload()} or {@link #extractEvaluationPayload()} ()} to get encapsulated data for the given type.
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
    private final List<Evaluation> evaluationsPayload;
    private final Evaluation evaluationPayload;

    private StatusEvent(EVENT_TYPE eventType, List<Evaluation> evaluationsPayload, Evaluation evaluation) {
        this.eventType = eventType;
        this.evaluationsPayload = evaluationsPayload;
        this.evaluationPayload = evaluation;
    }

    static StatusEvent makeSseStartEvent() {
        return new StatusEvent(EVENT_TYPE.SSE_START, null, null);
    }

    static StatusEvent makeSseResumeEvent() {
        return new StatusEvent(EVENT_TYPE.SSE_RESUME, null, null);
    }

    static StatusEvent makeSseEndEvent() {
        return new StatusEvent(EVENT_TYPE.SSE_END, null, null);
    }

    public StatusEvent(EVENT_TYPE eventType) {
        this.eventType = eventType;
        this.evaluationsPayload = null;
        this.evaluationPayload = null;
    }

    public StatusEvent(EVENT_TYPE eventType, List<Evaluation> evaluationsPayload) {
        this.eventType = eventType;
        this.evaluationsPayload = evaluationsPayload;
        this.evaluationPayload = null;
    }

    public StatusEvent(EVENT_TYPE eventType, Evaluation evaluation) {
        this.eventType = eventType;
        this.evaluationsPayload = null;
        this.evaluationPayload = evaluation;
    }

    public EVENT_TYPE getEventType() {
        return eventType;
    }

    public List<Evaluation> extractEvaluationListPayload() {
        assert evaluationsPayload != null;
        return evaluationsPayload;
    }

    public Evaluation extractEvaluationPayload() {
        assert evaluationPayload != null;
        return evaluationPayload;
    }

    /**
     * @deprecated
     * Since 1.2.0, use type-safe  {@link #extractEvaluationListPayload()} or {@link #extractEvaluationPayload()} ()} instead.
     * Will be removed in a future release
     * @return a list of Evaluations or a single Evaluation depending on {@link #getEventType()}
     */
    @Deprecated
    public Object extractPayload() {
        if (evaluationsPayload != null) return evaluationsPayload;
        return evaluationPayload;
    }

}
