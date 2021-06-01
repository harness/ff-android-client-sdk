package io.harness.cfsdk.cloud.analytics;

import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.concurrent.Executors;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.EventType;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.common.Destroyable;
import io.harness.cfsdk.logging.CfLog;

/**
 * This class handles various analytics service related components and prepares them 1) It creates
 * the LMAX ring buffer 2) It pushes data to the buffer and publishes it for consumption 3)
 * Initilazes the cache for analytics
 */
public class AnalyticsManager implements Destroyable {

    protected final Cache analyticsCache;

    private final Timer timer;
    private final String logTag;
    private final RingBuffer<Analytics> ringBuffer;

    {

        timer = new Timer();
        logTag = AnalyticsManager.class.getSimpleName();
    }

    public AnalyticsManager(

            final String environmentID,
            final String authToken,
            final CfConfiguration config
    ) {

        this.analyticsCache = AnalyticsCacheFactory.create(config.getAnalyticsCacheType());

        AnalyticsPublisherService analyticsPublisherService =
                new AnalyticsPublisherService(authToken, config, environmentID, analyticsCache);

        ringBuffer = createRingBuffer(config.getBufferSize(), analyticsPublisherService);

        final int frequency = config.getFrequency();
        final AnalyticsTimerTask timerTask = new AnalyticsTimerTask(ringBuffer);
        timer.schedule(

                timerTask,
                0L,
                frequency * 1000L
        );

        final String msg = String.format(

                "%s scheduled with frequency of: %s",
                AnalyticsTimerTask.class.getSimpleName(),
                frequency
        );
        CfLog.OUT.v(logTag, msg);
    }

    private RingBuffer<Analytics> createRingBuffer(

            int bufferSize,
            AnalyticsPublisherService analyticsPublisherService
    ) {

        // The factory for the event
        AnalyticsEventFactory factory = new AnalyticsEventFactory();

        // Construct the Disruptor
        Disruptor<Analytics> disruptor =
                new Disruptor<>(factory, bufferSize, Executors.newSingleThreadExecutor());

        // Connect the handler
        disruptor.handleEventsWith(getAnalyticsEventHandler(analyticsPublisherService));

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        return disruptor.getRingBuffer();
    }

    // push the incoming data to the ring buffer
    public void pushToQueue(Target target, FeatureConfig featureConfig, Variation variation) {

        Analytics analytics = new AnalyticsBuilder()
                .featureConfig(featureConfig)
                .target(target)
                .variation(variation)
                .eventType(EventType.METRICS)
                .build();

        long sequence = -1;
        try {

            sequence = ringBuffer.tryNext(); // Grab the next sequence
            Analytics event = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
            event.setFeatureConfig(analytics.getFeatureConfig());
            event.setTarget(analytics.getTarget());
            event.setVariation(analytics.getVariation());
        } catch (InsufficientCapacityException e) {

            CfLog.OUT.w(logTag, "Insufficient capacity in the analytics ringBuffer");
        } finally {
            if (sequence != -1) {

                ringBuffer.publish(sequence);
            }
        }
    }

    @Override
    public void destroy() {

        timer.cancel();
        timer.purge();
    }

    @NotNull
    protected AnalyticsEventHandler getAnalyticsEventHandler(

            AnalyticsPublisherService analyticsPublisherService
    ) {

        return new AnalyticsEventHandler(analyticsCache, analyticsPublisherService);
    }
}
