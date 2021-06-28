package io.harness.cfsdk;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.cloud.sse.SSEControlling;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockedCloudFactory;
import io.harness.cfsdk.mock.MockedFeatureRepository;
import io.harness.cfsdk.mock.MockedSSEController;

public class CfClientTest {

    private final String logTag;
    private final CloudFactory cloudFactory;

    {

        logTag = CfClientTest.class.getSimpleName();
        cloudFactory = new MockedCloudFactory();
    }

    @Mock
    Context context;

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
                true,
                false,
                10
        );

        final CountDownLatch latch = new CountDownLatch(1);
        final LinkedList<StatusEvent.EVENT_TYPE> events = new LinkedList<>();

        final EventsListener eventsListener = statusEvent -> {


            final StatusEvent.EVENT_TYPE type = statusEvent.getEventType();
            events.add(type);
            CfLog.OUT.v(logTag, "Event received: " + type);
            CfLog.OUT.v(logTag, "Events received: " + events.size());
        };

        final AtomicBoolean evaluationChanged = new AtomicBoolean();

        final EvaluationListener evaluationListener = evaluation -> {

            CfLog.OUT.v(logTag, "On evaluation");

            if (MockedFeatureRepository.MOCK_STRING.equals(evaluation.getIdentifier())) {

                evaluationChanged.set(true);
            }
        };

        boolean registerOk = cfClient.registerEventsListener(eventsListener);
        Assert.assertTrue(registerOk);

        registerOk = cfClient.registerEvaluationListener(

                MockedFeatureRepository.MOCK_STRING,
                evaluationListener
        );
        Assert.assertTrue(registerOk);

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

        final SSEControlling controlling = cloudFactory.sseController(null, null);
        Assert.assertTrue(controlling instanceof MockedSSEController);
        final MockedSSEController controller = (MockedSSEController) controlling;
        final EventsListener controllersListener = controller.getListener();
        Assert.assertNotNull(controllersListener);

        final Evaluation newEval = new Evaluation();
        newEval.setIdentifier(MockedFeatureRepository.MOCK_STRING);
        newEval.setFlag(MockedFeatureRepository.MOCK_STRING);
        newEval.setValue("");

        final StatusEvent event = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, newEval);
        controllersListener.onEventReceived(event);

        Assert.assertTrue(evaluationChanged.get());

        boolean unRegisterOk = cfClient.unregisterEvaluationListener(

                MockedFeatureRepository.MOCK_STRING,
                evaluationListener
        );
        Assert.assertTrue(unRegisterOk);

        unRegisterOk = cfClient.unregisterEventsListener(eventsListener);
        Assert.assertTrue(unRegisterOk);

        Assert.assertFalse(events.isEmpty());
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
