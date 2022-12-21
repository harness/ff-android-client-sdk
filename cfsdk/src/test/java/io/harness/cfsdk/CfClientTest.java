package io.harness.cfsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static io.harness.cfsdk.TestUtils.makeAuthResponse;
import static io.harness.cfsdk.TestUtils.makeBasicEvaluationsListJson;
import static io.harness.cfsdk.TestUtils.makeFlagCreateEvent;
import static io.harness.cfsdk.TestUtils.makeFlagDeleteEvent;
import static io.harness.cfsdk.TestUtils.makeMockJsonResponse;
import static io.harness.cfsdk.TestUtils.makeMockStreamResponse;
import static io.harness.cfsdk.TestUtils.makeServerUrl;
import static io.harness.cfsdk.TestUtils.makeSingleEvaluationJson;
import static io.harness.cfsdk.TestUtils.makeTargetSegmentCreateEvent;
import static io.harness.cfsdk.TestUtils.makeTargetSegmentPatchEvent;
import static io.harness.cfsdk.cloud.oksse.model.StatusEvent.EVENT_TYPE.EVALUATION_CHANGE;
import static io.harness.cfsdk.cloud.oksse.model.StatusEvent.EVENT_TYPE.EVALUATION_RELOAD;
import static io.harness.cfsdk.cloud.oksse.model.StatusEvent.EVENT_TYPE.EVALUATION_REMOVE;
import static io.harness.cfsdk.cloud.oksse.model.StatusEvent.EVENT_TYPE.SSE_END;
import static io.harness.cfsdk.cloud.oksse.model.StatusEvent.EVENT_TYPE.SSE_START;

import android.content.Context;

import androidx.annotation.NonNull;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockedCache;
import io.harness.cfsdk.mock.MockedNetworkInfoProvider;
import io.harness.cfsdk.utils.EventsListenerCounter;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Runs CfClient with a mocked ff-server
 */
public class CfClientTest {

    private static final String logTag = CfClientTest.class.getSimpleName();

    static class MockWebServerDispatcher extends Dispatcher {
        private final AtomicInteger version = new AtomicInteger(2);
        private final Map<String, Boolean> calledMap = new ConcurrentHashMap<>();
        @NonNull
        @Override
        public MockResponse dispatch(RecordedRequest request) {

            System.out.println("MOCK WEB SERVER GOT ------> " + request.getPath());

            calledMap.put(request.getPath(), true);

            switch (Objects.requireNonNull(request.getPath())) {
                case "/api/1.0/client/auth":
                    return makeAuthResponse();
                case "/api/1.0/client/env/00000000-0000-0000-0000-000000000000/target/anyone%40anywhere.com/evaluations?cluster=1":
                    return makeMockJsonResponse(200, makeBasicEvaluationsListJson());
                case "/api/1.0/stream?cluster=1":
                    return makeMockStreamResponse(200,
                            makeTargetSegmentCreateEvent("anyone@anywhere.com", version.getAndIncrement()),
                            makeTargetSegmentPatchEvent("anyone@anywhere.com", version.getAndIncrement()),
                            makeFlagCreateEvent("anyone@anywhere.com", version.getAndIncrement()),
                            makeFlagDeleteEvent("anyone@anywhere.com", version.getAndIncrement())
                    );
                case "/api/1.0/client/env/00000000-0000-0000-0000-000000000000/target/anyone%40anywhere.com/evaluations/anyone%40anywhere.com?cluster=1":
                    return  makeMockJsonResponse(200, makeSingleEvaluationJson());

            }

            throw new UnsupportedOperationException("ERROR: url not mapped " + request.getPath());
        }

        public void assertEndpointConnectionOrTimeout(int timeoutInSeconds, String url) throws InterruptedException {
            final int delayMs = 100;
            int maxWaitRemainingTime = (timeoutInSeconds*1000) / delayMs;
            while (!calledMap.containsKey(url) && maxWaitRemainingTime > 0) {
                System.out.println("Waiting for connection to " + url);
                Thread.sleep(delayMs);
                maxWaitRemainingTime--;
            }
            if (maxWaitRemainingTime == 0) {
                fail("Timed out");
            } else {
                System.out.println("Got a connection to " + url);
            }
        }
    }

    @Test
    public void shouldConnectToWebServerWithAbsoluteStreamUrl() throws Exception {
        testShouldConnectToWebServerSteamTest((host, port) -> CfConfiguration.builder()
                .baseUrl(makeServerUrl(host, port))
                .eventUrl(makeServerUrl(host, port))
                .streamUrl(makeServerUrl(host, port) + "/stream") // make sure we're still backwards compatible
                .enableAnalytics(false)
                .enableStream(true)
                .build());
    }

    @Test
    public void shouldConnectToWebServerWithoutStreamUrl() throws Exception {
        testShouldConnectToWebServerSteamTest((host, port) -> CfConfiguration.builder()
                .baseUrl(makeServerUrl(host, port))
                .eventUrl(makeServerUrl(host, port))
                // streamUrl not specified
                .enableAnalytics(false)
                .enableStream(true)
                .build());
    }

    private void testShouldConnectToWebServerSteamTest(BiFunction<String, Integer, CfConfiguration> configCallback) throws Exception {

        CfLog.testModeOn();

        final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            final CfClient client = new CfClient();
            client.setNetworkInfoProvider(new MockedNetworkInfoProvider());

            final CfConfiguration config = configCallback.apply(mockSvr.getHostName(), mockSvr.getPort());

            client.setNetworkInfoProvider(new MockedNetworkInfoProvider());
            final Target target = new Target().identifier("anyone@anywhere.com").name("unit-test");
            final Context mockContext = mock(Context.class);

            client.initialize(
                    mockContext,
                    "dummykey",
                    config,
                    target,
                    new MockedCache()
            );

            dispatcher.assertEndpointConnectionOrTimeout(30, "/api/1.0/stream?cluster=1");
        }
    }

    @Test
    public void testRegisterEventsListener() throws Exception {

        CfLog.testModeOn();

        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.setDispatcher(new MockWebServerDispatcher());
            mockSvr.start();

            final EventsListenerCounter eventCounter = new EventsListenerCounter(7); // Make sure this number matches assertions total below
            final CfClient client = new CfClient();
            client.setNetworkInfoProvider(new MockedNetworkInfoProvider());
            client.registerEventsListener(eventCounter);

            final CfConfiguration config = CfConfiguration.builder()
                    .baseUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .eventUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .enableAnalytics(false)
                    .enableStream(true)
                    .build();

            final Target target = new Target().identifier("anyone@anywhere.com").name("unit-test");
            final Context mockContext = mock(Context.class);

            client.initialize(
                    mockContext,
                    "dummykey",
                    config,
                    target,
                    new MockedCache()
            );

            eventCounter.waitForAllEventsOrTimeout(30);

            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(SSE_START));
            assertEquals((Long) 3L, (Long) eventCounter.getCountFor(EVALUATION_RELOAD));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_CHANGE));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_REMOVE));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(SSE_END));
            assertEquals((Long) 0L, (Long) eventCounter.getCountForUnknown());

        }
    }

    @Test
    public void evaluationReloadEventShouldSendCorrectPayload() throws InterruptedException {

        CfLog.testModeOn();

        final EventsListenerCounter eventCounter = new EventsListenerCounter(1);
        final CfClient client = new CfClient();
        client.registerEventsListener(eventCounter);

        client.sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, new Evaluation())); // invalid, will be filtered out
        client.sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, Collections.singletonList(new Evaluation()))); // valid
        eventCounter.waitForAllEventsOrTimeout(30);

        assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_RELOAD));
    }


}
