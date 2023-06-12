package io.harness.cfsdk.cloud.oksse;




import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

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
    public void onOpen(ServerSentEvent serverSentEvent, Response response, boolean isRescheduled) {
        if (this.eventsListener != null) {
            // SSE can be started from two entrypoints in CFClient:
            // 1. initialize
            // 2. reschedule
            // So make sure the right event is sent depending on how SSE has been started.
            if (isRescheduled) {
                this.eventsListener.onEventReceived(
                        new StatusEvent(StatusEvent.EVENT_TYPE.SSE_RESUME, serverSentEvent)
                );
            }
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


            Type evaluationsType = new TypeToken<ArrayList<Evaluation>>(){}.getType();
            ArrayList<Evaluation> evaluations = new ArrayList<>();
            evaluations.add(new Evaluation());
            evaluations.get(0).flag(identifier);

            // parse the actual evaluations sent in the sse event
            try {
                String evaluationJSON = jsonObject.getString("evaluations");
                Gson gson= new Gson();
                evaluations = gson.fromJson(evaluationJSON,evaluationsType);
            } catch (Exception e) {
                // this will happen if the evaluations aren't sent down the stream with the event
                // it's not an error case so no need to log it
                CfLog.OUT.e(logTag, String.format(
                        "Failed to parse evaluations array error=%s",
                        e.getMessage()
                ));
            }

            CfLog.OUT.v(

                    logTag,
                    String.format(

                            "onMessage(): domain=%s, eventType=%s, identifier=%s",
                            domain, eventType, identifier
                    )
            );

            StatusEvent statusEvent = null;
            if ("target-segment".equals(domain)) {
                // On creation, change or removal of a target group we want to reload evaluations
                if ("delete".equals(eventType) || "patch".equals(eventType) || "create".equals(eventType)) {
                    statusEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluations);
                }
            }

            if ("flag".equals(domain)) {
                // On creation or change of a flag we want to send a change event
                if ("create".equals(eventType) || "patch".equals(eventType)) {
                    statusEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, evaluations);
                }
                // On deletion of a flag we want to send a remove event
                if ("delete".equals(eventType)) {
                    statusEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, evaluations.get(0));
                }
            }

            if (statusEvent != null) {
                eventsListener.onEventReceived(statusEvent);
            } else {
                CfLog.OUT.e(logTag, String.format(
                        "Unrecognized Status Event received, Ignoring... onMessage(): domain=%s, eventType=%s, identifier=%s",
                        domain, eventType, identifier
                ));
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
