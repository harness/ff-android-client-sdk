package io.harness.cfsdk.cloud.analytics;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfClientException;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.EventType;
import io.harness.cfsdk.cloud.model.Target;

/**
 * This class handles various analytics service related components and prepares them 1) It creates
 * the LMAX ring buffer 2) It pushes data to the buffer and publishes it for consumption 3)
 * Initilazes the cache for analytics
 */
public class AnalyticsManager {

    private final Cache analyticsCache;
    private final RingBuffer<Analytics> ringBuffer;

    public AnalyticsManager(

            String environmentID,
            String apiKey,
            CfConfiguration config

    ) {

        this.analyticsCache = AnalyticsCacheFactory.create(config.getAnalyticsCacheType());
        AnalyticsPublisherService analyticsPublisherService =
                new AnalyticsPublisherService(apiKey, config, environmentID, analyticsCache);
        ringBuffer = createRingBuffer(config.getBufferSize(), analyticsPublisherService);
        ScheduledExecutorService timerExecutorService = Executors.newSingleThreadScheduledExecutor();
        TimerTask timerTask = new TimerTask(ringBuffer);
        timerExecutorService.scheduleAtFixedRate(

                timerTask,
                0,
                config.getFrequency(),
                TimeUnit.SECONDS
        );
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
        disruptor.handleEventsWith(
                new AnalyticsEventHandler(analyticsCache, analyticsPublisherService));

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

        long sequence = ringBuffer.next(); // Grab the next sequence
        try {

            Analytics event = ringBuffer.getPublished(sequence); // Get the entry in the Disruptor for the sequence
            event.setFeatureConfig(analytics.getFeatureConfig());
            event.setTarget(analytics.getTarget());
            event.setVariation(analytics.getVariation());
        } finally {

            ringBuffer.publish(sequence);
        }
    }
}
