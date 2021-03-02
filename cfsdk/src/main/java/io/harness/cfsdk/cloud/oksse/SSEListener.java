package io.harness.cfsdk.cloud.oksse;

import org.json.JSONException;
import org.json.JSONObject;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import okhttp3.Request;
import okhttp3.Response;

public class SSEListener implements ServerSentEvent.Listener {
    private EventsListener eventsListener;

    public SSEListener(EventsListener eventsListener) {
        this.eventsListener = eventsListener;
    }

    @Override
    public void onOpen(ServerSentEvent serverSentEvent, Response response) {
        if (this.eventsListener != null)
            this.eventsListener.onEventReceived(new StatusEvent(StatusEvent.EVENT_TYPE.SSE_START, serverSentEvent));
    }

    @Override
    public void onMessage(ServerSentEvent serverSentEvent, String id, String event, String message) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(message);
            String identifier = jsonObject.getString("identifier");
            String eventType = jsonObject.getString("event");
            Evaluation evaluation = new Evaluation();
            evaluation.flag(identifier);

            if ("create".equals(eventType) || "patch".equals(eventType)) {
                eventsListener.onEventReceived(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, evaluation));
            } else if ("delete".equals(eventType)) {
                eventsListener.onEventReceived(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, evaluation));
            }


        } catch (JSONException e) {
            e.printStackTrace();
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
        if (this.eventsListener != null) this.eventsListener.onEventReceived(new StatusEvent(StatusEvent.EVENT_TYPE.SSE_END, serverSentEvent));
    }

    @Override
    public Request onPreRetry(ServerSentEvent serverSentEvent, Request request) {
        return null;
    }
}
