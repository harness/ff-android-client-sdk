package io.harness.cfsdk.utils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.sse.EventsListener;
import io.harness.cfsdk.cloud.sse.StatusEvent;

public class EventsListenerCounter implements EventsListener {
    private static final Logger log = LoggerFactory.getLogger(EventsListenerCounter.class);
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
        log.debug("onEventReceived  ----------> type={}", name);

        switch (statusEvent.getEventType()) {
            case EVALUATION_RELOAD:
                assertNotNull(statusEvent.extractEvaluationListPayload());
                assertThat(statusEvent.extractEvaluationListPayload(), instanceOf(List.class)); // Fail the test if the payload has the wrong type
                map.merge(name, 1L, Long::sum);
                latch.countDown();
                break;
            case EVALUATION_CHANGE:
            case EVALUATION_REMOVE:
                assertNotNull(statusEvent.extractEvaluationPayload());
                assertThat(statusEvent.extractEvaluationPayload(), instanceOf(Evaluation.class));
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