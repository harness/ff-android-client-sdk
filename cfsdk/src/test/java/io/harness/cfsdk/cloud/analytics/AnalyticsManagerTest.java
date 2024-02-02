package io.harness.cfsdk.cloud.analytics;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.openapi.client.model.Variation;
import io.harness.cfsdk.cloud.openapi.metric.api.MetricsApi;

import static org.junit.Assert.*;

@SuppressWarnings("BusyWait")
public class AnalyticsManagerTest {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsManagerTest.class);
    private final int BUFFER_SIZE = 100;

    @BeforeClass
    public static void setupLogging() {
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);
    }

    @Test
    public void testPushToQueue() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(BUFFER_SIZE * BUFFER_SIZE);
        final AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        final CfConfiguration config = CfConfiguration.builder().metricsCapacity(BUFFER_SIZE).build();
        final MetricsApi metricsApi = Mockito.mock(MetricsApi.class);
        final AnalyticsPublisherService aps = new AnalyticsPublisherService(authInfo, metricsApi);
        final Target target = new Target().identifier("harness");

        final AnalyticsManager processor = new AnalyticsManager(config, target, aps) {
            @Override
            protected AnalyticsPublisherServiceCallback getSendingCallback() {
                return (success) -> {
                    if (success)
                        latch.countDown();
                };
            }
        };

        final ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(BUFFER_SIZE);
        final Variation variation = new Variation().identifier("true").value("true");

        for (int i = 1; i <= BUFFER_SIZE; i++) {
            final String name = "TEST THREAD " + i;
            WORKER_THREAD_POOL.submit(() -> {
                    Thread.currentThread().setName(name);
                    for (int j = 1; j <= BUFFER_SIZE; j++) {
                        processor.registerEvaluation("bool-flag", variation);

                        processor.flush();
                    }
                });
        }

        processor.flush();

        waitForAllMetricEventsToArrive(processor, BUFFER_SIZE * BUFFER_SIZE);
        assertEquals(BUFFER_SIZE * BUFFER_SIZE, processor.getMetricsSent());
    }

    private void waitForAllMetricEventsToArrive(AnalyticsManager processor, int totalMetricCount)
            throws InterruptedException {
        final int delayMs = 100;
        int maxWaitTime = 30_000 / delayMs;
        while (processor.getMetricsSent() < totalMetricCount && maxWaitTime > 0) {

            System.out.printf(
                    "Waiting for all metric events to arrive... totalMetricCount=%d metricsSent=%d mapSize=%d pending=%d\n",
                    totalMetricCount,
                    processor.getMetricsSent(),
                    processor.getQueueSize(),
                    processor.getPendingMetricsToBeSent());

            Thread.sleep(delayMs);
            maxWaitTime--;
        }

        if (maxWaitTime == 0) {
            fail("Timed out");
        }
    }

    @Test
    public void shouldNotThrowOutOfMemoryErrorWhenCreatingThreads() throws InterruptedException {

        final AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        final CfConfiguration config = CfConfiguration.builder().metricsCapacity(BUFFER_SIZE).build();
        final MetricsApi metricsApi = Mockito.mock(MetricsApi.class);
        final Target target = new Target().identifier("harness");
        final AnalyticsPublisherService aps = new AnalyticsPublisherService(authInfo, metricsApi);
        final AnalyticsManager processor = new AnalyticsManager(config, target, aps);

        final int FLAG_COUNT = 500;
        final int VARIATION_COUNT = 4;
        long maxQueueMapSize = 0;

        for (int f = 0; f < FLAG_COUNT; f++) {
            Variation feature = new Variation().identifier("bool-flag" + f);
            for (int v = 0; v < VARIATION_COUNT; v++) {
                Variation variation =
                        new Variation().identifier("true" + v).name("name" + v).value("true");

                processor.registerEvaluation(feature.getIdentifier(), variation);

                maxQueueMapSize = Math.max(maxQueueMapSize, processor.getQueueSize());
            }

            if (f % 10 == 0) {
                log.info(
                        "Metrics frequency map (cur: {} max: {}) Events sent ({}) Events pending ({})",
                        processor.getQueueSize(),
                        maxQueueMapSize,
                        processor.getMetricsSent(),
                        processor.getPendingMetricsToBeSent());

                processor.flush(); // mimic scheduled job
            }
        }


        processor.flush();

        int waitingForCount = FLAG_COUNT * VARIATION_COUNT;
        waitForAllMetricEventsToArrive(processor, waitingForCount);

        assertEquals(waitingForCount, processor.getMetricsSent());
    }
}
