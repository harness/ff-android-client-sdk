package io.harness.cfsdk.cloud.analytics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;

public class AnalyticsPublisherServiceTest {

    @Test
    public void shouldCallUnhappyPathWhenPostMetricsFails() throws ApiException {
        final AuthInfo authInfo = Mockito.mock(AuthInfo.class);
        final CfConfiguration config = CfConfiguration.builder().metricsCapacity(100).build();
        final MetricsApi metricsApi = Mockito.mock(MetricsApi.class);

        doThrow(new ApiException("dummy")).when(metricsApi).postMetrics(any(), any(), any());

        final AnalyticsPublisherService service = new AnalyticsPublisherService(config, authInfo, metricsApi);
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
        final CfConfiguration config = CfConfiguration.builder().metricsCapacity(100).build();
        final MetricsApi metricsApi = Mockito.mock(MetricsApi.class);

        doThrow(new ApiException("dummy")).when(metricsApi).postMetrics(anyString(), anyString(), any(Metrics.class));

        final AnalyticsPublisherService service = new AnalyticsPublisherService(config, authInfo, metricsApi);
        final AtomicBoolean result = new AtomicBoolean(false);

        service.sendData(new HashMap<>(), result::set);
        assertTrue(result.get());
    }

}
