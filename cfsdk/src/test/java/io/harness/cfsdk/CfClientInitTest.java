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

public class CfClientInitTest {

    @Mock
    Context context;

    private final String logTag;

    {

        logTag = CfClientInitTest.class.getSimpleName();
    }

    @Test
    public void initTestWithStream() {

        initTest(true);
    }

    @Test
    public void initTestNoStream() {

        initTest(false);
    }

    private void initTest(final boolean stream) {

        CfLog.testModeOn();

        final CloudFactory cloudFactory = new MockedCloudFactory();
        final CfClient cfClient = new CfClient(cloudFactory);

        final String mock = "mock";
        final String apiKey = String.valueOf(System.currentTimeMillis());

        final CountDownLatch latch = new CountDownLatch(1);

        final CfConfiguration cfConfiguration = new CfConfiguration(
                mock, mock, stream, false, 10
        );

        final AtomicBoolean success = new AtomicBoolean();

        cfClient.initialize(

                context,
                apiKey,
                cfConfiguration,
                new Target().identifier("target"),

                (info, result) -> {

                    Assert.assertNotNull(info);
                    Assert.assertNotNull(result);
                    Assert.assertTrue(result.isSuccess());
                    success.set(result.isSuccess());
                    latch.countDown();
                }
        );

        try {

            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }

        cfClient.destroy();
        Assert.assertTrue(success.get());
    }
}
