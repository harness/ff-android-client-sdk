package io.harness.cfsdk.cloud.oksse;




import org.json.JSONException;
import org.json.JSONObject;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.logging.CfLog;
import okhttp3.Request;
import okhttp3.Response;

public class SSEListener implements ServerSentEvent.Listener {

    private final String logTag;
    private final EventsListener eventsListener;

    {

        logTag = SSEListener.class.getSimpleName();
    }

    public SSEListener(EventsListener eventsListener) {

        this.eventsListener = eventsListener;
    }

    @Override
    public void onOpen(ServerSentEvent serverSentEvent, Response response) {
        if (this.eventsListener != null) {

            this.eventsListener.onEventReceived(
                    new StatusEvent(StatusEvent.EVENT_TYPE.SSE_START, serverSentEvent)
            );
        }
    }

    @Override
    public void onMessage(ServerSentEvent serverSentEvent, String id, String event, String message) {

        JSONObject jsonObject;
        try {

            jsonObject = new JSONObject(message);

            String domain = jsonObject.getString("domain");
            String eventType = jsonObject.getString("event");
            String identifier = jsonObject.getString("identifier");

            final Evaluation evaluation = new Evaluation();
            evaluation.flag(identifier);

            CfLog.OUT.v(

                    logTag,
                    String.format(

                            "onMessage(): domain=%s, eventType=%s, identifier=%s",
                            domain, eventType, identifier
                    )
            );

            if (
                    ("delete".equals(eventType) || "patch".equals(eventType))  &&
                            "target-segment".equals(domain)
            ) {

                final StatusEvent statusEvent =
                        new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluation);

                eventsListener.onEventReceived(statusEvent);

            } else if ("create".equals(eventType) || "patch".equals(eventType)) {

                final StatusEvent statusEvent =
                        new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, evaluation);

                eventsListener.onEventReceived(statusEvent);

            } else if ("delete".equals(eventType)) {

                final StatusEvent statusEvent =
                        new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, evaluation);

                eventsListener.onEventReceived(statusEvent);
            }

        } catch (JSONException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
    }

    @Override
    public void onComment(ServerSentEvent serverSentEvent, String s) {

    }

    @Override
    public boolean onRetryTime(ServerSentEvent serverSentEvent, long l) {
        return false;
    }

    @Override
    public boolean onRetryError(
            ServerSentEvent serverSentEvent, Throwable throwable, Response response) {
        return false;
    }

    @Override
    public void onClosed(ServerSentEvent serverSentEvent) {
        if (this.eventsListener != null)
            this.eventsListener.onEventReceived(new StatusEvent(StatusEvent.EVENT_TYPE.SSE_END, serverSentEvent));
    }

    @Override
    public Request onPreRetry(ServerSentEvent serverSentEvent, Request request) {
        return null;
    }
}
