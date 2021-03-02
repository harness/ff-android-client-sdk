package io.harness.cfsdk;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.ApiResponse;
import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.NetworkInfoProvider;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.AuthenticationResponse;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.sse.SSEController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.eq;

public class CFClientTest {

    @Mock CloudFactory cloudFactory;
    @Mock Cloud cloud;
    @Mock Context context;
    @Mock SSEController sseController;
    @Mock FeatureRepository featureRepository;
    @Mock EvaluationPolling polling;
    @Mock NetworkInfoProvider networkInfoProvider;
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cloudFactory.cloud(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(cloud);
        Mockito.when(cloudFactory.sseController()).thenReturn(sseController);
        Mockito.when(cloudFactory.getFeatureRepository(any(), any())).thenReturn(featureRepository);
        Mockito.when(cloudFactory.evaluationPolling(10, TimeUnit.SECONDS)).thenReturn(polling);
        Mockito.when(cloudFactory.networkInfoProvider(any())).thenReturn(networkInfoProvider);
    }

    private void initTestSetup() {
        Mockito.when(cloud.getAuthInfo()).thenReturn(new AuthInfo("", "", "", "", "", ""));
        Mockito.when(cloud.isInitialized()).thenReturn(true);
        Mockito.when(cloud.initialize()).thenReturn(true);

        SSEConfig sseConfig = new SSEConfig("demo_url", "demo_token");
        Mockito.when(cloud.getConfig()).thenReturn(sseConfig);
        Mockito.when(networkInfoProvider.isNetworkAvailable()).thenReturn(true);
    }

    @Test
    public void initTestWithStream() {
        initTestSetup();

        CFClient cfClient = new CFClient(cloudFactory);
        cfClient.initialize(context, "",
                new CFConfiguration("", "demo_url", true, 10, "target"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Mockito.verify(cloud, Mockito.times(1)).initialize();
        Mockito.verify(featureRepository, Mockito.times(1)).getAllEvaluations("","target", false);
        Mockito.verify(sseController, Mockito.times(1)).start(any(), any());

        cfClient.destroy();
        Mockito.verify(polling, Mockito.times(1)).stop();
        Mockito.verify(featureRepository, Mockito.times(1)).clear();

    }

    @Test
    public void initTestNoStream() {
        initTestSetup();
        CFClient cfClient = new CFClient(cloudFactory);
        cfClient.initialize(context, "",
                new CFConfiguration("", "", false, 10, "target"));
        cfClient.boolVariation("","", false);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Mockito.verify(cloud, Mockito.times(1)).initialize();
        Mockito.verify(sseController, Mockito.times(0)).start(any(), any());
        Mockito.verify(featureRepository, Mockito.times(1)).getAllEvaluations("","target", false);

    }

    @Test
    public void listenerTest() {
        initTestSetup();

        StatusEvent sseStartEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_START, null);
        StatusEvent sseEndEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_END, null);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ((EventsListener) invocation.getArgument(1)).onEventReceived(sseStartEvent);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((EventsListener) invocation.getArgument(1)).onEventReceived(sseEndEvent);
                return null;
            }
        }).when(sseController).start(any(), any());

        CFClient cfClient = new CFClient(cloudFactory);

        EventsListener eventsListener = Mockito.mock(EventsListener.class);
        Mockito.doNothing().when(eventsListener).onEventReceived(Mockito.any());
        cfClient.registerEventsListener(eventsListener);

        cfClient.initialize(context, "",
                new CFConfiguration("", "", true, 10, "target"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Mockito.verify(eventsListener, Mockito.times(1)).onEventReceived(sseStartEvent);
        Mockito.verify(eventsListener, Mockito.times(1)).onEventReceived(sseEndEvent);
        Mockito.verify(polling, Mockito.times(1)).stop();

    }


    @Test
    public void initVariations() {
        initTestSetup();

        CFClient cfClient = new CFClient(cloudFactory);
        cfClient.initialize(context, "",
                new CFConfiguration("", "demo_url", false, 10, "target"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Evaluation evaluation = new Evaluation();
        evaluation.flag("string_eval");
        evaluation.value("string_val");
        Mockito.when(featureRepository.getEvaluation(Mockito.anyString(), Mockito.anyString(), Mockito.eq("string_eval"), Mockito.eq(true)))
                .thenReturn(evaluation);

        String eval = cfClient.stringVariation("string_eval", "target", "string_eval");

        Evaluation boolEvaluation = new Evaluation();
        boolEvaluation.flag("bool_eval");
        boolEvaluation.value(true);
        Mockito.when(featureRepository.getEvaluation(Mockito.anyString(), Mockito.anyString(), Mockito.eq("bool_eval"), Mockito.eq(true)))
                .thenReturn(boolEvaluation);

        boolean boolEvalValue = cfClient.boolVariation("bool_eval", "target", false);

        Evaluation intEvaluation = new Evaluation();
        intEvaluation.flag("int_evaluation");
        intEvaluation.value(5);
        Mockito.when(featureRepository.getEvaluation(Mockito.anyString(), Mockito.anyString(), Mockito.eq("int_evaluation"), Mockito.eq(true)))
                .thenReturn(intEvaluation);

        double intEvalValue = cfClient.numberVariation("int_evaluation", "target", 1);
        double emptyEvalValue = cfClient.numberVariation("empty_eval", "target", 1);

        Assert.assertEquals(eval, "string_val");
        Assert.assertTrue(boolEvalValue);
        Assert.assertEquals((int)intEvalValue, 5);
        Assert.assertEquals((int)emptyEvalValue, 1);
    }
}
