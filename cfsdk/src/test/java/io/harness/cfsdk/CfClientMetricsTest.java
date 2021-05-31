package io.harness.cfsdk;

import android.content.Context;

import org.junit.Test;
import org.mockito.Mock;

import io.harness.cfsdk.logging.CfLog;

public class CfClientMetricsTest {

    @Mock
    Context context;

    private final String logTag;

    {

        logTag = CfClientInitTest.class.getSimpleName();
    }

    @Test
    public void testMetrics(){

        CfLog.testModeOn();


    }
}
