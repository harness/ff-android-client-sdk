package io.harness.cfsdk.cloud.analytics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.openapi.metric.ApiException;
import io.harness.cfsdk.cloud.openapi.client.model.Variation;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.openapi.metric.api.MetricsApi;
import io.harness.cfsdk.cloud.openapi.metric.model.Metrics;

public class AnalyticsPublisherServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsPublisherServiceTest.class);

    @Test
    public void shouldCallUnhappyPathWhenPostMetricsFails() throws ApiException {
        final AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        final MetricsApi metricsApi = Mockito.mock(MetricsApi.class);

        doThrow(new ApiException("dummy")).when(metricsApi).postMetrics(any(), any(), any());

        final AnalyticsPublisherService service = new AnalyticsPublisherService(authInfo, metricsApi);
        final AtomicBoolean result = new AtomicBoolean(true);

        final Analytics analytics = new AnalyticsBuilder()
                .target(new Target().identifier("dummy"))
                .evaluationId("dummy")
                .variation(new Variation())
                .build();

        Map<Analytics, Long> payload = new HashMap<>();
        payload.put(analytics, 1L);

        service.sendData(payload, result::set);
        assertFalse(result.get());
    }

    @Test
    public void shouldCallHappyPathWhenPostMetricsHasNoData() throws ApiException {
        final AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        final MetricsApi metricsApi = Mockito.mock(MetricsApi.class);

        doThrow(new ApiException("dummy")).when(metricsApi).postMetrics(anyString(), anyString(), any(Metrics.class));

        final AnalyticsPublisherService service = new AnalyticsPublisherService(authInfo, metricsApi);
        final AtomicBoolean result = new AtomicBoolean(false);

        service.sendData(new HashMap<>(), result::set);
        assertTrue(result.get());
    }

    @Test
    public void shouldNotPostMetricsIfTotalSumIsZero() throws ApiException {
        final AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        final MetricsApi metricsApi = Mockito.mock(MetricsApi.class);

        final AnalyticsPublisherService service = new AnalyticsPublisherService(authInfo, metricsApi);

        final Map<Analytics, Long> freqMap = new HashMap<>();
        final Target target = new Target().identifier("dummy1");
        freqMap.put(new Analytics(target, "dummy", new Variation().identifier("variation1")), 0L);
        freqMap.put(new Analytics(target, "dummy2", new Variation().identifier("variation2")), 0L);
        freqMap.put(new Analytics(target, "dummy3", new Variation().identifier("variation3")), 0L);

        service.sendData(freqMap, (AnalyticsPublisherServiceCallback) success -> log.info("sendData success={}", success));

        // Verify that we did not post anything
        verify(metricsApi, times(0)).postMetrics(anyString(), anyString(), any(Metrics.class));
    }

}
