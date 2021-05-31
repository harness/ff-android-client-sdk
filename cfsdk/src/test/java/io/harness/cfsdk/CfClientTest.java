package io.harness.cfsdk;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockedCloudFactory;
import io.harness.cfsdk.mock.MockedFeatureRepository;

public class CfClientTest {

    private final CloudFactory cloudFactory;

    {

        cloudFactory = new MockedCloudFactory();
    }

    @Mock
    Context context;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void listenerTest() {

        initTestSetup();

        final AtomicBoolean initOk = new AtomicBoolean();

        final String mock = "mock";
        final CfClient cfClient = new CfClient(cloudFactory);
        final String apiKey = String.valueOf(System.currentTimeMillis());

        final CfConfiguration cfConfiguration = new CfConfiguration(

                mock,
                mock,
                false,
                false,
                10
        );

        CountDownLatch latch = new CountDownLatch(1);

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

            latch.await();
        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(initOk.get());

        cfClient.destroy();
    }

    @Test
    public void initVariations() {

        initTestSetup();

        final AtomicBoolean initOk = new AtomicBoolean();

        final String mock = "mock";
        final CfClient cfClient = new CfClient(cloudFactory);
        final String apiKey = String.valueOf(System.currentTimeMillis());

        final CfConfiguration cfConfiguration = new CfConfiguration(

                mock,
                mock,
                false,
                false,
                10
        );

        CountDownLatch latch = new CountDownLatch(1);

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

            latch.await();
        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(initOk.get());

        final String stringEval = cfClient.stringVariation(

                MockedFeatureRepository.MOCK_STRING,
                ""
        );

        final boolean boolEval = cfClient.boolVariation(

                MockedFeatureRepository.MOCK_BOOL,
                false
        );

        final double numberEval = cfClient.numberVariation(

                MockedFeatureRepository.MOCK_NUMBER,
                0
        );

        final String noStringEval = cfClient.stringVariation(mock, "");
        final boolean noBoolEval = cfClient.boolVariation(mock, false);
        final double noNumberEval = cfClient.numberVariation(mock, 0);


        Assert.assertEquals(stringEval, MockedFeatureRepository.MOCK_STRING);
        Assert.assertTrue(boolEval);
        Assert.assertEquals((int) numberEval, MockedFeatureRepository.MOCK_NUMBER.length());

        Assert.assertEquals("", noStringEval);
        Assert.assertEquals(0, (int) noNumberEval);
        Assert.assertFalse(noBoolEval);

        cfClient.destroy();
    }

    private void initTestSetup() {

        CfLog.testModeOn();
    }
}
