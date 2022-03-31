package io.harness.cfsdk.cloud.analytics;

import org.junit.Test;

import io.harness.cfsdk.logging.CfLog;

public class AnalyticsManagerTest {

    private final String logTag;

    {

        logTag = AnalyticsManagerTest.class.getSimpleName();
    }

    @Test
    public void testAnalyticsManager() {

        CfLog.testModeOn();

        CfLog.OUT.v(logTag, "Testing: " + AnalyticsManager.class.getSimpleName());

        // TODO:
    }
}
