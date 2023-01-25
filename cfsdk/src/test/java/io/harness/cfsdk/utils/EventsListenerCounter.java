package io.harness.cfsdk.utils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfClientTest;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.logging.CfLog;

public class EventsListenerCounter implements EventsListener {
    private static final String logTag = CfClientTest.class.getSimpleName().toUpperCase();
    private final CountDownLatch latch;
    private final int numOfValidEventsToWaitFor;
    private final Map<String, Long> map = new ConcurrentHashMap<>();

    public EventsListenerCounter(int numOfValidEventsToWaitFor) {
        latch = new CountDownLatch(numOfValidEventsToWaitFor);
        this.numOfValidEventsToWaitFor = numOfValidEventsToWaitFor;
        Arrays.stream(StatusEvent.EVENT_TYPE.values()).forEach(e -> map.put(e.name(), 0L));
        map.put("UNKNOWN", 0L);
    }

    public void waitForAllEventsOrTimeout(int timeoutInSeconds) throws InterruptedException {
        if (!latch.await(timeoutInSeconds, TimeUnit.SECONDS)) {
            fail(String.format("Expected %d events but only got %d", numOfValidEventsToWaitFor, latch.getCount()));
        }
    }

    @Override
    public void onEventReceived(StatusEvent statusEvent) {
        final String name = statusEvent.getEventType().name();
        final String payloadInfo = (statusEvent.extractPayload() == null) ? "NULL" : statusEvent.extractPayload().getClass().getSimpleName();
        CfLog.OUT.i(logTag, String.format("onEventReceived  ----------> type=%s payload=%s", name, payloadInfo));

        switch (statusEvent.getEventType()) {
            case EVALUATION_RELOAD:
                assertNotNull(statusEvent.extractPayload());
                assertThat(statusEvent.extractPayload(), instanceOf(List.class)); // Fail the test if the payload has the wrong type
                map.merge(name, 1L, Long::sum);
                latch.countDown();
                break;
            case EVALUATION_CHANGE:
            case EVALUATION_REMOVE:
                assertNotNull(statusEvent.extractPayload());
                assertThat(statusEvent.extractPayload(), instanceOf(Evaluation.class));
                map.merge(name, 1L, Long::sum);
                latch.countDown();
                break;
            case SSE_START:
            case SSE_RESUME:

            case SSE_END:
                map.merge(name, 1L, Long::sum);
                latch.countDown();
                break;

            default:
                map.merge("UNKNOWN", 1L, Long::sum);
                break;
        }
    }

    public Long getCountFor(StatusEvent.EVENT_TYPE event) {
        return (Long) map.get(event.name());
    }

    public Long getCountForUnknown() {
        return (Long) map.get("UNKNOWN");
    }
}