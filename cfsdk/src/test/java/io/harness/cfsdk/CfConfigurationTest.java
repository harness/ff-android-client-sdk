package io.harness.cfsdk;

import org.junit.Test;

import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.Mockito.*;

public class CfConfigurationTest {

    @Test
    public void shouldBuildConfigCorrectlyViaBuilder() {

        X509Certificate mockX509 = mock(X509Certificate.class);

        CfConfiguration config = new CfConfiguration.Builder()
                .baseUrl("https://dummy_base_url:1234")
                .eventUrl("https://dummy_event_url:1234")
                .streamUrl("https://dummy_stream_url:1234")
                .enableAnalytics(false)
                .enableStream(false)
                .pollingInterval(60)
                .tlsTrustedCAs(Collections.singletonList(mockX509))
                .metricsPublishingAcceptableDurationInMillis(1234)
                .metricsPublishingIntervalInMillis(60001)
                .metricsCapacity(123)
                .build();

        assertEquals("https://dummy_base_url:1234", config.getBaseURL());
        assertEquals("https://dummy_event_url:1234", config.getEventURL());
        assertEquals("https://dummy_stream_url:1234", config.getStreamURL());
        assertFalse(config.isAnalyticsEnabled());
        assertFalse(config.getStreamEnabled());
        assertEquals(60, config.getPollingInterval());
        assertEquals(1, config.getTlsTrustedCAs().size());
        assertNotNull(config.getTlsTrustedCAs().get(0));
        assertThat(config.getTlsTrustedCAs().get(0), instanceOf(X509Certificate.class));
        assertEquals(1234, config.getMetricsServiceAcceptableDurationInMillis());
        assertEquals(60001, config.getMetricsPublishingIntervalInMillis());
        assertEquals(123, config.getMetricsCapacity());
    }

}
