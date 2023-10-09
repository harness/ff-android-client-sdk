package io.harness.cfsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static io.harness.cfsdk.CfConfiguration.DEFAULT_METRICS_CAPACITY;
import static io.harness.cfsdk.TestUtils.makeAuthResponse;
import static io.harness.cfsdk.TestUtils.makeBasicEvaluationsListJson;
import static io.harness.cfsdk.TestUtils.makeEmptyEvaluationsListJson;
import static io.harness.cfsdk.TestUtils.makeFlagCreateEvent;
import static io.harness.cfsdk.TestUtils.makeFlagDeleteEvent;
import static io.harness.cfsdk.TestUtils.makeMockJsonResponse;
import static io.harness.cfsdk.TestUtils.makeMockStreamResponse;
import static io.harness.cfsdk.TestUtils.makeSecureServerUrl;
import static io.harness.cfsdk.TestUtils.makeServerUrl;
import static io.harness.cfsdk.TestUtils.makeSingleEvaluationJson;
import static io.harness.cfsdk.TestUtils.makeSuccessResponse;
import static io.harness.cfsdk.TestUtils.makeTargetSegmentCreateEvent;
import static io.harness.cfsdk.TestUtils.makeTargetSegmentPatchEvent;
import static io.harness.cfsdk.cloud.sse.StatusEvent.EVENT_TYPE.EVALUATION_CHANGE;
import static io.harness.cfsdk.cloud.sse.StatusEvent.EVENT_TYPE.EVALUATION_RELOAD;
import static io.harness.cfsdk.cloud.sse.StatusEvent.EVENT_TYPE.EVALUATION_REMOVE;
import static io.harness.cfsdk.cloud.sse.StatusEvent.EVENT_TYPE.SSE_END;
import static io.harness.cfsdk.cloud.sse.StatusEvent.EVENT_TYPE.SSE_RESUME;
import static io.harness.cfsdk.cloud.sse.StatusEvent.EVENT_TYPE.SSE_START;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.AtomicLongMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.sse.EventsListener;
import io.harness.cfsdk.cloud.sse.StatusEvent;
import io.harness.cfsdk.mock.MockedCache;
import io.harness.cfsdk.mock.MockedNetworkInfoProvider;
import io.harness.cfsdk.utils.EventsListenerCounter;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;

/**
 * Runs CfClient with a mocked ff-server
 */
public class CfClientTest {

    private static final Logger log = LoggerFactory.getLogger(CfClientTest.class);

    private static final Target DUMMY_TARGET = new Target().identifier("anyone@anywhere.com").name("unit-test");

    static class MockWebServerDispatcher extends Dispatcher {

        public static final String AUTH_ENDPOINT = "/api/1.0/client/auth";
        public static final String EVALUATION_ENDPOINT = "/api/1.0/client/env/00000000-0000-0000-0000-000000000000/target/anyone%40anywhere.com/evaluations/anyone%40anywhere.com?cluster=1";
        public static final String ALL_EVALUATIONS_ENDPOINT = "/api/1.0/client/env/00000000-0000-0000-0000-000000000000/target/anyone%40anywhere.com/evaluations?cluster=1";
        public static final String STREAM_ENDPOINT = "/api/1.0/stream?cluster=1";
        public static final String METRICS_ENDPOINTS = "/api/1.0/metrics/00000000-0000-0000-0000-000000000000?cluster=1";

        private final AtomicInteger version = new AtomicInteger(2);
        protected final AtomicLongMap<String> calledMap = AtomicLongMap.create();

        @NonNull
        @Override
        public MockResponse dispatch(RecordedRequest request) {

            System.out.println("MOCK WEB SERVER GOT ------> " + request.getPath());

            calledMap.incrementAndGet(Objects.requireNonNull(request.getPath()));

            switch (Objects.requireNonNull(request.getPath())) {
                case AUTH_ENDPOINT:
                    return makeAuthResponse();
                case ALL_EVALUATIONS_ENDPOINT:
                    return makeMockJsonResponse(200, makeBasicEvaluationsListJson());
                case STREAM_ENDPOINT:
                    return makeMockStreamResponse(200,
                            makeTargetSegmentCreateEvent("anyone@anywhere.com", version.getAndIncrement()),
                            makeTargetSegmentPatchEvent("anyone@anywhere.com", version.getAndIncrement()),
                            makeFlagCreateEvent("anyone@anywhere.com", version.getAndIncrement()),
                            makeFlagDeleteEvent("anyone@anywhere.com", version.getAndIncrement())
                    );
                case EVALUATION_ENDPOINT:
                    return makeMockJsonResponse(200, makeSingleEvaluationJson());
                case METRICS_ENDPOINTS:
                    return makeSuccessResponse();
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

        public int getUrlAccessCount(String url) {
            return (int) calledMap.get(url);
        }

    }

    static class EvalEndpointDispatcher_WithCustomHttpCode_AndEmptyEvalList extends CfClientTest.MockWebServerDispatcher {

        private final int httpCodeForEvaluationEndpoint;
        EvalEndpointDispatcher_WithCustomHttpCode_AndEmptyEvalList(int httpCodeForEvaluationEndpoint) {
            this.httpCodeForEvaluationEndpoint = httpCodeForEvaluationEndpoint;
        }

        @NonNull @Override
        public MockResponse dispatch(RecordedRequest request) {
            if (EVALUATION_ENDPOINT.equals(request.getPath())) {
                calledMap.incrementAndGet(Objects.requireNonNull(request.getPath()));
                return makeMockJsonResponse(httpCodeForEvaluationEndpoint, makeSingleEvaluationJson("testFlag", "boolean", "true", "anyone@anywhere.com"));
            } else if (ALL_EVALUATIONS_ENDPOINT.equals(request.getPath())) {
                calledMap.incrementAndGet(Objects.requireNonNull(request.getPath()));
                return makeMockJsonResponse(200, makeEmptyEvaluationsListJson());
            }
            return super.dispatch(request);
        }
    }

    static class EvalEndpointDispatcher_ForCacheMiss extends EvalEndpointDispatcher_WithCustomHttpCode_AndEmptyEvalList {
        EvalEndpointDispatcher_ForCacheMiss() {
            super(200);
        }
    }

    static class EvalEndpointDispatcher_ReturnsHttp400 extends EvalEndpointDispatcher_WithCustomHttpCode_AndEmptyEvalList {
        EvalEndpointDispatcher_ReturnsHttp400() {
            super(400);
        }
    }

    static class EvalEndpointDispatcher_ReturnsHttp500 extends EvalEndpointDispatcher_WithCustomHttpCode_AndEmptyEvalList {
        EvalEndpointDispatcher_ReturnsHttp500() {
            super(500);
        }
    }


    @Test
    public void shouldConnectToWebServerWithAbsoluteStreamUrl() throws Exception {
        testShouldConnectToWebServerStreamTest((host, port) -> CfConfiguration.builder()
                .baseUrl(makeServerUrl(host, port))
                .eventUrl(makeServerUrl(host, port))
                .streamUrl(makeServerUrl(host, port) + "/stream") // make sure we're still backwards compatible
                .enableAnalytics(false)
                .enableStream(true)
                .build());
    }

    @Test
    public void shouldConnectToWebServerWithoutStreamUrl() throws Exception {
        testShouldConnectToWebServerStreamTest((host, port) -> CfConfiguration.builder()
                .baseUrl(makeServerUrl(host, port))
                .eventUrl(makeServerUrl(host, port))
                // streamUrl not specified
                .enableAnalytics(false)
                .enableStream(true)
                .build());
    }

    private void testShouldConnectToWebServerStreamTest(BiFunction<String, Integer, CfConfiguration> configCallback) throws Exception {

        final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            final CfClient client = new CfClient();
            client.setNetworkInfoProvider(MockedNetworkInfoProvider.create());

            final CfConfiguration config = configCallback.apply(mockSvr.getHostName(), mockSvr.getPort());
            client.setNetworkInfoProvider(MockedNetworkInfoProvider.create());

            final Context mockContext = mock(Context.class);

            client.initialize(
                    mockContext,
                    "dummykey",
                    config,
                    DUMMY_TARGET,
                    new MockedCache()
            );

            dispatcher.assertEndpointConnectionOrTimeout(30, "/api/1.0/stream?cluster=1");
        }
    }

    @Test
    public void testRegisterEventsListener() throws Exception {
        final MockWebServerDispatcher dispatcher = new EvalEndpointDispatcher_ForCacheMiss();
        final MockedCache cache = new MockedCache();


        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            final EventsListenerCounter eventCounter = new EventsListenerCounter(7); // Make sure this number matches assertions total below
            final CfClient client = new CfClient();
            client.setNetworkInfoProvider(MockedNetworkInfoProvider.create());
            client.registerEventsListener(eventCounter);

            final CfConfiguration config = CfConfiguration.builder()
                    .baseUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .eventUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .enableAnalytics(false)
                    .enableStream(true)
                    .build();

            final Context mockContext = mock(Context.class);

            client.initialize(
                    mockContext,
                    "dummykey",
                    config,
                    DUMMY_TARGET,
                    cache
            );

            eventCounter.waitForAllEventsOrTimeout(30);

            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(SSE_START));
            assertEquals((Long) 3L, (Long) eventCounter.getCountFor(EVALUATION_RELOAD));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_CHANGE));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_REMOVE));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(SSE_END));
            assertEquals((Long) 0L, (Long) eventCounter.getCountForUnknown());


            // An SSE flag update should cause the evaluations endpoint to be queried, and the cache to be updated once
            // There will be no cache hits on that target since it will always go out to the server (SSE events don't include the actual state)
            assertEquals(1, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
            assertEquals(1, cache.getCacheSavedCountForEvaluation("anyone@anywhere.com"));
            assertEquals(0, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));


        }
    }

    @Test
    public void evaluationReloadEventShouldSendCorrectPayload() throws InterruptedException {

        final EventsListenerCounter eventCounter = new EventsListenerCounter(1);
        final CfClient client = new CfClient();
        client.registerEventsListener(eventCounter);

        client.sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, new Evaluation())); // invalid, will be filtered out
        client.sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, Collections.singletonList(new Evaluation()))); // valid
        eventCounter.waitForAllEventsOrTimeout(30);

        assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_RELOAD));
    }

    @Test
    public void sseResumeEventShouldSendCorrectPayload() throws InterruptedException {

        final EventsListenerCounter eventCounter = new EventsListenerCounter(1);
        final CfClient client = new CfClient();
        client.registerEventsListener(eventCounter);

        client.sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.SSE_RESUME, new Evaluation())); // invalid, will be filtered out
        client.sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.SSE_RESUME, Collections.singletonList(new Evaluation()))); // valid
        eventCounter.waitForAllEventsOrTimeout(30);

        assertEquals((Long) 1L, (Long) eventCounter.getCountFor(SSE_RESUME));
    }

    /*
     * Set network off (MockedNetworkInfoProvider.createWithNetworkOff())
     * Manually pre-populate cache (since getAllEvaluations won't be called)
     * Get some boolean variations
     * Assert no network calls are done (checking counters in MockWebServerDispatcher)
     * Assert cache was used (checking counters in MockedCache)
     */
    @Test
    public void shouldGetFlag_FromCacheAlways_WhenNetworkOffline() throws Exception {
        final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
        final MockedCache cache = new MockedCache();

        cache.saveEvaluation("Production_anyone@anywhere.com", "anyone@anywhere.com", new Evaluation().value("true"));

        runEvaluation_WithClientCallback(dispatcher, cache, MockedNetworkInfoProvider.createWithNetworkOff(), client -> {
            for (int i = 0; i < 60; i++) {
                boolean eval = client.boolVariation("anyone@anywhere.com", false);
                assertTrue(eval);
            }
        });

        assertEquals(0, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
        assertEquals(60, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));

    }

    /*
     * Same as above, but with network on (MockedNetworkInfoProvider.create())
     */
    @Test
    public void shouldGetFlag_FromCacheAlways_WhenNetworkOnline() throws Exception {
        final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
        final MockedCache cache = new MockedCache();

        runEvaluation_WithClientCallback(dispatcher, cache, MockedNetworkInfoProvider.create(), client -> {
            for (int i = 0; i < 60; i++) {
                boolean eval = client.boolVariation("anyone@anywhere.com", false);
                assertTrue(eval);
            }
        });

        assertEquals(0, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
        assertEquals(60, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));
    }

    /*
     * First check in cache and return null (empty MockedCache)
     * Evaluation endpoint API should be called (MockedNetworkInfoProvider)
     * Result should be saved in cache
     */
    @Test
    public void shouldGetFlag_FromNetwork_WhenNotInCache() throws Exception {
        final MockWebServerDispatcher dispatcher = new EvalEndpointDispatcher_ForCacheMiss();
        final MockedCache cache = new MockedCache();

        runEvaluation_WithClientCallback(dispatcher, cache, MockedNetworkInfoProvider.create(), client -> {
            boolean eval = client.boolVariation("anyone@anywhere.com", false);
            assertTrue(eval);
        });

        assertEquals(1, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
        assertEquals(0, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));
        assertEquals(1, cache.getCacheSavedCountForEvaluation("anyone@anywhere.com"));
    }

    /*
     * Same as above but calls getEvaluationById() directly
     */
    @Test
    public void shouldGetEvaluation_FromNetwork_WhenNotInCache() throws Exception {
        final MockWebServerDispatcher dispatcher = new EvalEndpointDispatcher_ForCacheMiss();
        final MockedCache cache = new MockedCache();

        runEvaluation_WithClientCallback(dispatcher, cache, MockedNetworkInfoProvider.create(), client -> {

            Target target = new Target();
            target.identifier("anyone@anywhere.com");

            Evaluation eval = client.getEvaluationById("anyone@anywhere.com", target, false);
            assertNotNull(eval);
            assertEquals("testFlag", eval.getFlag());
            assertEquals("anyone@anywhere.com", eval.getIdentifier());
            assertEquals("true", eval.getValue());
        });

        assertEquals(1, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
        assertEquals(0, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));
        assertEquals(1, cache.getCacheSavedCountForEvaluation("anyone@anywhere.com"));
    }

    /*
     * First check in cache and return null (empty MockedCache)
     * Evaluation endpoint API should be called and return 400 (MockedNetworkInfoProvider)
     * Default value should be served
     */
    @Test
    public void shouldGetEvaluation_WithDefaultValue_WhenNotInCache_AndServerReturns400() throws Exception {
        final MockWebServerDispatcher dispatcher = new EvalEndpointDispatcher_ReturnsHttp400();
        final MockedCache cache = new MockedCache();
        final String DEFAULT_VALUE = "false";

        runEvaluation_WithClientCallback(dispatcher, cache, MockedNetworkInfoProvider.create(), client -> {

            Target target = new Target();
            target.identifier("anyone@anywhere.com");

            Evaluation eval = client.getEvaluationById("anyone@anywhere.com", target, DEFAULT_VALUE);
            assertNotNull(eval);
            assertEquals("anyone@anywhere.com", eval.getFlag());
            assertEquals(DEFAULT_VALUE, eval.getValue());
        });

        assertEquals(1, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
        assertEquals(0, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));
        assertEquals(0, cache.getCacheSavedCountForEvaluation("anyone@anywhere.com"));
    }

    /*
     * Same as above, except evaluation endpoint API should be called and return 500
     */
    @Test
    public void shouldGetEvaluation_WithDefaultValue_WhenNotInCache_AndServerReturns500() throws Exception {
        final MockWebServerDispatcher dispatcher = new EvalEndpointDispatcher_ReturnsHttp500();
        final MockedCache cache = new MockedCache();
        final String DEFAULT_VALUE = "false";

        runEvaluation_WithClientCallback(dispatcher, cache, MockedNetworkInfoProvider.create(), client -> {

            Target target = new Target();
            target.identifier("anyone@anywhere.com");

            Evaluation eval = client.getEvaluationById("anyone@anywhere.com", target, DEFAULT_VALUE);
            assertNotNull(eval);
            assertEquals("anyone@anywhere.com", eval.getFlag());
            assertEquals(DEFAULT_VALUE, eval.getValue());
        });

        assertEquals(1, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
        assertEquals(0, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));
        assertEquals(0, cache.getCacheSavedCountForEvaluation("anyone@anywhere.com"));
    }

    /*
     * Tests config item tlsTrustedCAs() with a self signed cert. We want to see default, stream
     * and metrics APIs called at least once (post handshake) when TLS is enabled.
     */
    @Test
    public void testSdkWithCustomTlsCert() throws Exception {

        final HeldCertificate localCert = new HeldCertificate.Builder()
                .addSubjectAlternativeName("localhost")
                .addSubjectAlternativeName("127.0.0.1")
                .build();
        final HandshakeCertificates serverCertificates = new HandshakeCertificates.Builder()
                .heldCertificate(localCert)
                .build();

        log.debug("Using self-signed cert: {}\n", localCert.certificatePem());

        final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.useHttps(serverCertificates.sslSocketFactory(), false);
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();
            final String url = makeSecureServerUrl(mockSvr.getHostName(), mockSvr.getPort());
            log.debug("mock TLS server running on {}:{}", mockSvr.getHostName(), mockSvr.getPort());

            final CfClient client = new CfClient();
            client.setNetworkInfoProvider(MockedNetworkInfoProvider.create());

            final CfConfiguration config = mock(CfConfiguration.class);
            when(config.getBaseURL()).thenReturn(url);
            when(config.getStreamURL()).thenReturn(url + "/stream");
            when(config.getEventURL()).thenReturn(url);
            when(config.isAnalyticsEnabled()).thenReturn(true);
            when(config.getStreamEnabled()).thenReturn(true);
            when(config.getMetricsPublishingIntervalInMillis()).thenReturn(1000L); // Force the publish time to be within the timeout
            when(config.getMetricsCapacity()).thenReturn(DEFAULT_METRICS_CAPACITY);
            when(config.getTlsTrustedCAs()).thenReturn(Collections.singletonList(localCert.certificate()));

            client.setNetworkInfoProvider(MockedNetworkInfoProvider.create());

            final Context mockContext = mock(Context.class);
            final CountDownLatch authLatch = new CountDownLatch(1);

            client.initialize(
                    mockContext,
                    "dummykey",
                    config,
                    DUMMY_TARGET,
                    new MockedCache(),
                    (authInfo, result) -> {
                        if (result.isSuccess())
                            authLatch.countDown();
                        else
                            result.getError().printStackTrace();
                    }
            );

            assertTrue("auth did not succeed", authLatch.await(30, TimeUnit.SECONDS));

            client.boolVariation("anyone@anywhere.com", false); // need at least 1 eval for metrics to push

            dispatcher.assertEndpointConnectionOrTimeout(30, MockWebServerDispatcher.AUTH_ENDPOINT);
            dispatcher.assertEndpointConnectionOrTimeout(30, MockWebServerDispatcher.STREAM_ENDPOINT);
            dispatcher.assertEndpointConnectionOrTimeout(30, MockWebServerDispatcher.METRICS_ENDPOINTS);
        }
    }


    private void runEvaluation_WithClientCallback(MockWebServerDispatcher dispatcher, MockedCache cache, NetworkInfoProviding networkInfoProvider, Consumer<CfClient> callback) throws Exception {
        runEvaluation(dispatcher, cache, networkInfoProvider, null, callback, false);
    }

    private void runEvaluation_WithEventsCallback(MockWebServerDispatcher dispatcher, MockedCache cache, NetworkInfoProviding networkInfoProvider, EventsListener eventListener) throws Exception {
        runEvaluation(dispatcher, cache, networkInfoProvider, eventListener, null, true);
    }

    private void runEvaluation(MockWebServerDispatcher dispatcher, MockedCache cache, NetworkInfoProviding networkInfoProvider, EventsListener eventListener, Consumer<CfClient> callback, boolean streamEnabled) throws Exception {

        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            final CfClient client = CfClient.getInstance();
            client.reset();
            client.setNetworkInfoProvider(networkInfoProvider);
            client.registerEventsListener(eventListener);

            final CfConfiguration config = CfConfiguration.builder()
                    .baseUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .eventUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .enableAnalytics(false)
                    .enableStream(streamEnabled)
                    .build();

            final Context mockContext = mock(Context.class);
            final CountDownLatch authLatch = new CountDownLatch(1);

            client.initialize(
                    mockContext,
                    "dummykey",
                    config,
                    DUMMY_TARGET,
                    cache, (authInfo, result) -> authLatch.countDown()
            );

            assertTrue(authLatch.await(30, TimeUnit.SECONDS));
            assertEquals(0, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));

            log.debug("Auth completed");

            if (callback != null) {
                callback.accept(client);
            }
        }

    }

}
