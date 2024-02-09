package io.harness.cfsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static io.harness.cfsdk.CfClientTest.MockWebServerDispatcher.ALL_EVALUATIONS_ENDPOINT;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.AtomicLongMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.network.NewRetryInterceptor;
import io.harness.cfsdk.cloud.openapi.client.ApiClient;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;
import io.harness.cfsdk.cloud.openapi.metric.model.Metrics;
import io.harness.cfsdk.cloud.sse.EventsListener;
import io.harness.cfsdk.cloud.sse.StatusEvent;
import io.harness.cfsdk.mock.MockedCache;
import io.harness.cfsdk.mock.MockedNetworkInfoProvider;
import io.harness.cfsdk.utils.EventsListenerCounter;
import okhttp3.OkHttpClient;
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
        private final List<Evaluation> moreEvaluations;
        protected final AtomicLongMap<String> calledMap = AtomicLongMap.create();

        MockWebServerDispatcher() {
            this(new ArrayList<>());
        }
        MockWebServerDispatcher(List<Evaluation> moreEvaluations) {
            this.moreEvaluations = moreEvaluations;
        }

        @NonNull
        @Override
        public MockResponse dispatch(RecordedRequest request) {

            System.out.println("MOCK WEB SERVER GOT ------> " + request.getPath());

            calledMap.incrementAndGet(Objects.requireNonNull(request.getPath()));

            switch (Objects.requireNonNull(request.getPath())) {
                case AUTH_ENDPOINT:
                    return makeAuthResponse();
                case ALL_EVALUATIONS_ENDPOINT:
                    return makeMockJsonResponse(200, makeBasicEvaluationsListJson(moreEvaluations));
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

        public void assertEndpointConnectionOrTimeout(int timeoutInSeconds, String url, int expectedConnectionCount) throws InterruptedException {
            final int delayMs = 100;
            int maxWaitRemainingTime = (timeoutInSeconds*1000) / delayMs;
            while (calledMap.get(url) != expectedConnectionCount && maxWaitRemainingTime > 0) {
                System.out.println("Waiting for connection to " + url + " got " + calledMap.get(url) + " of " + expectedConnectionCount);
                Thread.sleep(delayMs);
                maxWaitRemainingTime--;
            }
            if (maxWaitRemainingTime == 0) {
                fail("Timed out");
            } else {
                System.out.println("Got a connection to " + url + " " + calledMap.get(url) + " of " + expectedConnectionCount);
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

    @Test
    public void shouldConnectToWebServerWithAbsoluteStreamUrl() throws Exception {
        testShouldConnectToWebServerStreamTest((host, port) -> CfConfiguration.builder()
                .baseUrl(makeServerUrl(host, port))
                .eventUrl(makeServerUrl(host, port))
                .streamUrl(makeServerUrl(host, port) + "/stream") // make sure we're still backwards compatible
                .enableAnalytics(false)
                .enableStream(true)
                .cache(new MockedCache())
                .debug(true)
                .build());
    }

    @Test
    public void variationMethodsShouldNotReturnDefaults() throws Exception {
        variationMethodsShouldNotReturnDefaults((host, port) -> CfConfiguration.builder()
                .baseUrl(makeServerUrl(host, port))
                .eventUrl(makeServerUrl(host, port))
                .streamUrl(makeServerUrl(host, port) + "/stream") // make sure we're still backwards compatible
                .enableAnalytics(false)
                .enableStream(true)
                .cache(new MockedCache())
                .debug(true)
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
                .cache(new MockedCache())
                .debug(true)
                .build());
    }

    private void testShouldConnectToWebServerStreamTest(BiFunction<String, Integer, CfConfiguration> configCallback) throws Exception {

        final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            final CfClient client = new CfClient();

            final CfConfiguration config = configCallback.apply(mockSvr.getHostName(), mockSvr.getPort());

            client.initialize(
                    makeMockContextWithNetworkOnline(),
                    "dummykey",
                    config,
                    DUMMY_TARGET
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

            final EventsListenerCounter eventCounter = new EventsListenerCounter(11); // Make sure this number matches assertions total below
            final CfClient client = new CfClient();
            client.registerEventsListener(eventCounter);

            final CfConfiguration config = CfConfiguration.builder()
                    .baseUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .eventUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .enableAnalytics(false)
                    .enableStream(true)
                    .cache(cache)
                    .debug(true)
                    .build();

            client.initialize(
                    makeMockContextWithNetworkOnline(),
                    "dummykey",
                    config,
                    DUMMY_TARGET
            );

            eventCounter.waitForAllEventsOrTimeout(30);

            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(SSE_START));
            assertEquals((Long) 7L, (Long) eventCounter.getCountFor(EVALUATION_RELOAD));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_CHANGE));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(EVALUATION_REMOVE));
            assertEquals((Long) 1L, (Long) eventCounter.getCountFor(SSE_END));
            assertEquals((Long) 0L, (Long) eventCounter.getCountForUnknown());


            Thread.sleep(1000);

            // An SSE flag update should cause the evaluations endpoint to be queried, and the cache to be updated once
            // There will be no cache hits on that target since it will always go out to the server (SSE events don't include the actual state)
            assertEquals(1, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
            assertEquals(1, cache.getCacheSavedCountForEvaluation("testFlag"));
            assertEquals(0, cache.getCacheHitCountForEvaluation("testFlag"));
        }
    }

    /*
     * Same as above, but with network on
     */
    @Test
    public void shouldGetFlag_FromCacheAlways_WhenNetworkOnline() throws Exception {
        final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
        final MockedCache cache = new MockedCache();

        runEvaluation_WithClientCallback(dispatcher, cache, makeMockContextWithNetworkOnline(), client -> {
            for (int i = 0; i < 60; i++) {
                boolean eval = client.boolVariation("testFlag", false);
                assertTrue(eval);
            }
        });

        assertEquals(0, dispatcher.getUrlAccessCount(MockWebServerDispatcher.EVALUATION_ENDPOINT));
        assertEquals(60, cache.getCacheHitCountForEvaluation("testFlag"));
    }

    private CloudCache makeMockCache() {
        final CloudCache cache = new MockedCache();
        cache.saveEvaluation("Production", "anyone@anywhere.com", new Evaluation().value("dummy"));
        return cache;
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
            final CfConfiguration config = mock(CfConfiguration.class);
            when(config.getBaseURL()).thenReturn(url);
            when(config.getStreamURL()).thenReturn(url + "/stream");
            when(config.getEventURL()).thenReturn(url);
            when(config.isAnalyticsEnabled()).thenReturn(true);
            when(config.isStreamEnabled()).thenReturn(true);
            when(config.getMetricsPublishingIntervalInMillis()).thenReturn(1000L); // Force the publish time to be within the timeout
            when(config.getMetricsCapacity()).thenReturn(DEFAULT_METRICS_CAPACITY);
            when(config.getTlsTrustedCAs()).thenReturn(Collections.singletonList(localCert.certificate()));
            when(config.getCache()).thenReturn(makeMockCache());
            when(config.isDebugEnabled()).thenReturn(true);

            client.initialize(
                    makeMockContextWithNetworkOnline(),
                    "dummykey",
                    config,
                    DUMMY_TARGET
            );

            assertTrue(client.waitForInitialization(30_000));

            for (int i = 0; i < 10; i++) {
                client.boolVariation("anyone@anywhere.com", false); // need at least 1 eval for metrics to push
                MILLISECONDS.sleep(100);
            }

            dispatcher.assertEndpointConnectionOrTimeout(30, MockWebServerDispatcher.AUTH_ENDPOINT);
            dispatcher.assertEndpointConnectionOrTimeout(30, MockWebServerDispatcher.STREAM_ENDPOINT);
            dispatcher.assertEndpointConnectionOrTimeout(30, MockWebServerDispatcher.METRICS_ENDPOINTS);
        }
    }




    private void runEvaluation_WithClientCallback(MockWebServerDispatcher dispatcher, MockedCache cache, Context context, Consumer<CfClient> callback) throws Exception {
        runEvaluation(dispatcher, cache, context, null, callback, false);
    }

    private void runEvaluation_WithEventsCallback(MockWebServerDispatcher dispatcher, MockedCache cache, Context context, EventsListener eventListener) throws Exception {
        runEvaluation(dispatcher, cache, context, eventListener, null, true);
    }

    private void runEvaluation(MockWebServerDispatcher dispatcher, MockedCache cache, Context context, EventsListener eventListener, Consumer<CfClient> callback, boolean streamEnabled) throws Exception {

        try (MockWebServer mockSvr = new MockWebServer()) {
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            final CfClient client = new CfClient();
            client.registerEventsListener(eventListener);

            final CfConfiguration config = CfConfiguration.builder()
                    .baseUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .eventUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .enableAnalytics(false)
                    .enableStream(streamEnabled)
                    .cache(cache)
                    .debug(true)
                    .build();

            client.initialize(
                    context,
                    "dummykey",
                    config,
                    DUMMY_TARGET
            );

            assertTrue(client.waitForInitialization(30_000));
            assertEquals(0, cache.getCacheHitCountForEvaluation("anyone@anywhere.com"));

            log.debug("Auth completed");

            if (callback != null) {
                callback.accept(client);
            }
        }
    }

    private void variationMethodsShouldNotReturnDefaults(BiFunction<String, Integer, CfConfiguration> configCallback) throws JSONException, IOException {

        final List<Evaluation> extraEvals = Arrays.asList(
            new Evaluation().flag("bool1").kind("boolean").value("true").identifier("b1"),
            new Evaluation().flag("string1").kind("string").value("str").identifier("s1"),
            new Evaluation().flag("number1").kind("number").value("123").identifier("n1"),
            new Evaluation().flag("json1").kind("json").value("{'flag':'on'}").identifier("j1")
        );

        try (MockWebServer mockSvr = new MockWebServer()) {
            final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher(extraEvals);
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            final CfConfiguration config = configCallback.apply(mockSvr.getHostName(), mockSvr.getPort());
            final CfClient client = new CfClient();
            client.initialize(
                    makeMockContextWithNetworkOnline(),
                    "dummykey",
                    config,
                    DUMMY_TARGET
            );

            client.waitForInitialization(30_000);

            boolean boolResult = client.boolVariation("bool1", false);
            String strResult = client.stringVariation("string1", "");
            double numResult = client.numberVariation("number1", 0);
            JSONObject jsonResult = client.jsonVariation("json1", new JSONObject("{}"));

            assertTrue(boolResult);
            assertEquals("str", strResult);
            assertEquals(123, numResult, .0);
            assertNotNull("default (or wrong) json returned", jsonResult.get("flag"));
            assertEquals("on", jsonResult.get("flag"));
        }
    }


    @Test
    public void refreshEvalsShouldOnlyPollFirstCallThenSkip() throws InterruptedException, IOException {
        try (MockWebServer mockSvr = new MockWebServer()) {
            final MockWebServerDispatcher dispatcher = new MockWebServerDispatcher();
            mockSvr.setDispatcher(dispatcher);
            mockSvr.start();

            AuthInfo authInfo = mock(AuthInfo.class);
            when(authInfo.getEnvironmentIdentifier()).thenReturn("dummy1");
            when(authInfo.getCluster()).thenReturn("dummy3");

            final CfConfiguration config = CfConfiguration.builder()
                    .baseUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .eventUrl(makeServerUrl(mockSvr.getHostName(), mockSvr.getPort()))
                    .enableAnalytics(false)
                    .enableStream(false)
                    .cache(new MockedCache())
                    .build();

            CfClient client = new CfClient();
            client.initialize(makeMockContextWithNetworkOnline(), "dummyapikey", config, DUMMY_TARGET);
            client.waitForInitialization(30_000);
            client.refreshEvaluations();

            for (int i = 0; i < 1000; i++) {
                client.refreshEvaluations();
            }

            // Assert soft polling works correctly
            // getAllEvaluations should only be invoked once every 60s even if caller spams the method
            // 2 for counting first poll after auth
            dispatcher.assertEndpointConnectionOrTimeout(30, ALL_EVALUATIONS_ENDPOINT, 2);
            assertEquals(2, dispatcher.getUrlAccessCount(ALL_EVALUATIONS_ENDPOINT)); // auth, first poll
        }
    }


    private Context makeMockContextWithNetwork(boolean networkEnabled) {
        final NetworkInfo networkInfo = mock(NetworkInfo.class);
        when(networkInfo.isConnected()).thenReturn(networkEnabled);

        final ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);

        final Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);

        return mockContext;
    }

    private Context makeMockContextWithNetworkOnline() {
        return makeMockContextWithNetwork(true);
    }

    private Context makeMockContextWithNetworkOffline() {
        return makeMockContextWithNetwork(false);
    }
}
