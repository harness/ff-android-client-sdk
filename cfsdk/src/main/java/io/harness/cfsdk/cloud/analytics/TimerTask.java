package io.harness.cfsdk.cloud.analytics;

import com.lmax.disruptor.RingBuffer;

import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.model.EventType;
import io.harness.cfsdk.logging.CfLog;

public class TimerTask implements Runnable {

    private final String logTag;
    private final RingBuffer<Analytics> ringBuffer;

    {

        logTag = TimerTask.class.getSimpleName();
    }

    public TimerTask(RingBuffer<Analytics> ringBuffer) {

        this.ringBuffer = ringBuffer;
    }

    @Override
    public void run() {

        long sequence = ringBuffer.next(); // Grab the next sequence
        try {

            CfLog.OUT.i(logTag, "Publishing timerInfo to ringBuffer");
            Analytics event = ringBuffer.getPublished(sequence); // Get the entry in the Disruptor for the sequence
            event.setEventType(EventType.TIMER);
        } finally {

            ringBuffer.publish(sequence);
        }
    }
}
