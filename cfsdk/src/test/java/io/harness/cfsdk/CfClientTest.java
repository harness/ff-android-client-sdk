package io.harness.cfsdk;

import android.content.Context;

import com.google.common.cache.Cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.network.NetworkInfoProvider;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.SSEAuthentication;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.sse.SSEController;
import io.harness.cfsdk.logging.CfLog;

import static org.mockito.ArgumentMatchers.any;

public class CfClientTest {

    private final String logTag;

    {

        logTag = CfClientTest.class.getSimpleName();
    }

    @Mock
    CloudFactory cloudFactory;
    @Mock
    Cloud cloud;
    @Mock
    Context context;
    @Mock
    SSEController sseController;
    @Mock
    FeatureRepository featureRepository;
    @Mock
    EvaluationPolling polling;
    @Mock
    NetworkInfoProvider networkInfoProvider;
    @Mock
    Cache<String, FeatureConfig> featureCache;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        Mockito.when(cloudFactory.cloud(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(cloud);

        Mockito.when(cloudFactory.sseController(cloud, cloud.getAuthInfo(), featureCache)).thenReturn(sseController);
        Mockito.when(cloudFactory.getFeatureRepository(any(), any())).thenReturn(featureRepository);
        Mockito.when(cloudFactory.evaluationPolling(10, TimeUnit.SECONDS)).thenReturn(polling);
        Mockito.when(cloudFactory.networkInfoProvider(any())).thenReturn(networkInfoProvider);
    }

    private void initTestSetup() {

        CfLog.testModeOn();

        Mockito.when(cloud.getAuthInfo()).thenReturn(new AuthInfo("", "", "", "", "", "", ""));
        Mockito.when(cloud.isInitialized()).thenReturn(true);
        Mockito.when(cloud.initialize()).thenReturn(true);

        SSEConfig sseConfig = new SSEConfig("demo_url", new SSEAuthentication("demo_token", "demo_api_token"));
        Mockito.when(cloud.getConfig()).thenReturn(sseConfig);
        Mockito.when(networkInfoProvider.isNetworkAvailable()).thenReturn(true);
    }

    @Test
    public void listenerTest() {

        initTestSetup();

        CountDownLatch latch = new CountDownLatch(2);
        CountDownLatch unregisterLatch = new CountDownLatch(1);
        CountDownLatch finalLatch = new CountDownLatch(1);

        StatusEvent sseStartEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_START, null);
        StatusEvent sseEndEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_END, null);

        Evaluation payload = new Evaluation();
        payload.flag("demo_change");
        payload.setValue("demo_value");
        Mockito.when(
                featureRepository.getEvaluation(Mockito.anyString(), Mockito.anyString(), Mockito.eq("demo_change"), Mockito.anyBoolean())
        ).thenReturn(payload);
        StatusEvent evaluationChangeEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, payload);


        Evaluation removePayload = new Evaluation();
        removePayload.setFlag("demo_remove");
        Mockito.doNothing().when(featureRepository).remove(
                Mockito.anyString(), Mockito.anyString(), Mockito.eq("demo_remove")
        );

        StatusEvent evaluationRemoveEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, removePayload);

        Mockito.doAnswer(invocation -> {
            try {

                Thread.sleep(1000);
            } catch (InterruptedException e) {

                CfLog.OUT.e(logTag, e.getMessage(), e);
            }

            ((EventsListener) invocation.getArgument(1)).onEventReceived(sseStartEvent);
            try {

                Thread.sleep(1000);
            } catch (InterruptedException e) {

                CfLog.OUT.e(logTag, e.getMessage(), e);
            }
            ((EventsListener) invocation.getArgument(1)).onEventReceived(sseEndEvent);

            try {

                Thread.sleep(2000);
            } catch (InterruptedException e) {

                CfLog.OUT.e(logTag, e.getMessage(), e);
            }

            ((EventsListener) invocation.getArgument(1)).onEventReceived(evaluationRemoveEvent);
            try {

                Thread.sleep(2000);
            } catch (InterruptedException e) {

                CfLog.OUT.e(logTag, e.getMessage(), e);
            }

            ((EventsListener) invocation.getArgument(1)).onEventReceived(evaluationChangeEvent);

            latch.countDown();

            unregisterLatch.await(5, TimeUnit.SECONDS);
            ((EventsListener) invocation.getArgument(1)).onEventReceived(evaluationChangeEvent);

            finalLatch.countDown();
            return null;
        }).when(sseController).start(any(), any());

        CfClient cfClient = new CfClient(cloudFactory);

        EventsListener eventsListener = Mockito.mock(EventsListener.class);
        Mockito.doNothing().when(eventsListener).onEventReceived(Mockito.any());
        boolean registerOk = cfClient.registerEventsListener(eventsListener);
        Assert.assertTrue(registerOk);

        EvaluationListener evaluationListener = Mockito.mock(EvaluationListener.class);

        Mockito.doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(evaluationListener).onEvaluation(any());

        registerOk = cfClient.registerEvaluationListener("demo_change", evaluationListener);
        Assert.assertTrue(registerOk);

        EvaluationListener newListener = Mockito.mock(EvaluationListener.class);
        Mockito.doNothing().when(newListener).onEvaluation(Mockito.any());

        registerOk = cfClient.registerEvaluationListener("demo_change", evaluationListener);
        Assert.assertFalse(registerOk);

        CfConfiguration cfConfiguration = new CfConfiguration("", "", true, false, 10);

        cfClient.initialize(context, "", cfConfiguration, new Target().identifier("target"));

        try {

            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }

        Mockito.verify(eventsListener, Mockito.times(1)).onEventReceived(sseStartEvent);
        Mockito.verify(eventsListener, Mockito.times(1)).onEventReceived(sseEndEvent);
        Mockito.verify(polling, Mockito.times(1)).stop();

        Mockito.verify(evaluationListener, Mockito.times(1)).onEvaluation(payload);
        Mockito.verify(featureRepository, Mockito.times(1)).getEvaluation(
                Mockito.anyString(), Mockito.anyString(), Mockito.eq("demo_change"), Mockito.eq(false));

        Mockito.verify(featureRepository, Mockito.times(1)).remove(
                Mockito.anyString(), Mockito.anyString(), Mockito.eq("demo_remove"));

        boolean unregisterOk = cfClient.unregisterEvaluationListener("demo_change", evaluationListener);
        Assert.assertTrue(unregisterOk);
        unregisterLatch.countDown();

        try {

            finalLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }

        Mockito.verify(evaluationListener, Mockito.times(1)).onEvaluation(payload);

        unregisterOk = cfClient.unregisterEventsListener(eventsListener);
        Assert.assertTrue(unregisterOk);
    }


    @Test
    public void initVariations() {

        initTestSetup();

        CountDownLatch latch = new CountDownLatch(1);

        CfClient cfClient = new CfClient(cloudFactory);
        CfConfiguration cfConfiguration = new CfConfiguration("", "demo_url", false, false, 10);

        cfClient.initialize(

                context,
                "",
                cfConfiguration,
                new Target().identifier("target"),
                (info, result) -> {

                    Assert.assertNotNull(info);
                    Assert.assertNotNull(result);
                    Assert.assertTrue(result.isSuccess());
                    latch.countDown();
                }
        );

        try {

            Thread.sleep(3000);
        } catch (InterruptedException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }

        Evaluation evaluation = new Evaluation();
        evaluation.flag("string_eval");
        evaluation.value("string_val");
        Mockito.when(featureRepository.getEvaluation(Mockito.anyString(), Mockito.anyString(), Mockito.eq("string_eval"), Mockito.eq(true)))
                .thenReturn(evaluation);

        String eval = cfClient.stringVariation("string_eval", "string_eval");

        Evaluation boolEvaluation = new Evaluation();
        boolEvaluation.flag("bool_eval");
        boolEvaluation.value(true);
        Mockito.when(featureRepository.getEvaluation(Mockito.anyString(), Mockito.anyString(), Mockito.eq("bool_eval"), Mockito.eq(true)))
                .thenReturn(boolEvaluation);

        boolean boolEvalValue = cfClient.boolVariation("bool_eval", false);

        Evaluation intEvaluation = new Evaluation();
        intEvaluation.flag("int_evaluation");
        intEvaluation.value(5);
        Mockito.when(featureRepository.getEvaluation(Mockito.anyString(), Mockito.anyString(), Mockito.eq("int_evaluation"), Mockito.eq(true)))
                .thenReturn(intEvaluation);

        double intEvalValue = cfClient.numberVariation("int_evaluation", 1);
        double emptyEvalValue = cfClient.numberVariation("empty_eval", 1);

        Assert.assertEquals(eval, "string_val");
        Assert.assertTrue(boolEvalValue);
        Assert.assertEquals((int) intEvalValue, 5);
        Assert.assertEquals((int) emptyEvalValue, 1);
    }
}
