package io.harness.cfsdk.cloud.analytics;

import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;

import java.util.TimerTask;

import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.model.EventType;
import io.harness.cfsdk.logging.CfLog;

public class AnalyticsTimerTask extends TimerTask {

    private final String logTag;
    private final RingBuffer<Analytics> ringBuffer;

    {

        logTag = AnalyticsTimerTask.class.getSimpleName();
    }

    public AnalyticsTimerTask(RingBuffer<Analytics> ringBuffer) {

        this.ringBuffer = ringBuffer;
    }

    @Override
    public void run() {

        CfLog.OUT.v(logTag, "run: START");
        long sequence = -1;
        try {

            sequence = ringBuffer.tryNext(); // Grab the next sequence if we can
            CfLog.OUT.d(logTag, "Publishing timerInfo to ringBuffer");
            Analytics event = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
            event.setEventType(EventType.TIMER);
        } catch (InsufficientCapacityException e) {

            CfLog.OUT.w(logTag, "Insufficient capacity in the analytics ringBuffer");
        } finally {
            if (sequence != -1) {

                ringBuffer.publish(sequence);
            }
        }
        CfLog.OUT.v(logTag, "run: END");
    }
}
