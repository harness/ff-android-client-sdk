package io.harness.cfsdk;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockedCloudFactory;

public class CfClientMetricsTest {

    @Mock
    Context context;

    private final String logTag;
    private final CloudFactory cloudFactory;

    {

        cloudFactory = new MockedCloudFactory();
        logTag = CfClientInitTest.class.getSimpleName();
    }

    @Test
    public void testMetrics(){

        CfLog.testModeOn();

        final String mock = "mock";
        final CfClient cfClient = new CfClient(cloudFactory);
        final String apiKey = String.valueOf(System.currentTimeMillis());

        final CfConfiguration cfConfiguration = new CfConfiguration(

                mock,
                mock,
                true,
                true,
                10
        );

        final AtomicBoolean initOk = new AtomicBoolean();
        final CountDownLatch latch = new CountDownLatch(1);

        cfClient.initialize(

                context,
                apiKey,
                cfConfiguration,
                new Target().identifier("target"),

                (info, result) -> {

                    Assert.assertNotNull(info);
                    Assert.assertNotNull(result);
                    Assert.assertTrue(result.isSuccess());
                    initOk.set(result.isSuccess());
                    latch.countDown();
                }
        );

        try {

            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }

        Assert.assertTrue(initOk.get());

        // TODO: Test metrics

        cfClient.destroy();
    }
}
