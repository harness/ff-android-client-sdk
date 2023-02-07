package io.harness.cfsdk;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.SocketPolicy;

public class TestUtils {

    static class Event {
        public Event(String event, String domain, String identifier, int version) {
            this.event = event;
            this.domain = domain;
            this.identifier = identifier;
            this.version = version;
        }

        final String event, domain, identifier;
        final int version;
    }

    static MockResponse makeMockJsonResponse(int httpCode, String body) {
        return new MockResponse()
                .setResponseCode(httpCode)
                .setBody(body)
                .addHeader("Content-Type", "application/json; charset=UTF-8");
    }

    static String makeBasicEvaluationsListJson() {
        final List<Evaluation> list = new ArrayList<>();
        list.add(new Evaluation().flag("testFlag").kind("boolean").value("true").identifier("anyone@anywhere.com"));

        return new Gson().toJson(list);
    }

    static String makeEmptyEvaluationsListJson() {
        final List<Evaluation> list = new ArrayList<>();
        return new Gson().toJson(list);
    }

    static String makeSingleEvaluationJson(String flag, String kind, String value, String id) {
        final Evaluation eval = new Evaluation().flag(flag).kind(kind).value(value).identifier(id);
        return new Gson().toJson(eval);
    }

    static String makeSingleEvaluationJson() {
        final Evaluation eval = new Evaluation().flag("testFlag").kind("boolean").value("true").identifier("anyone@anywhere.com");
        return new Gson().toJson(eval);
    }

    static MockResponse makeAuthResponse() {
        return makeMockJsonResponse(200, "{\"authToken\": \"" + makeDummyJwtToken() + "\"}");
    }

    static Event makeTargetSegmentPatchEvent(String identifier, int version) {
        return makeEvent("patch", "target-segment", identifier, version);
    }

    static Event makeTargetSegmentCreateEvent(String identifier, int version) {
        return makeEvent("create", "target-segment", identifier, version);
    }

    static Event makeFlagCreateEvent(String identifier, int version) {
        return makeEvent("create", "flag", identifier, version);
    }

    static Event makeFlagDeleteEvent(String identifier, int version) {
        return makeEvent("delete", "flag", identifier, version);
    }

    static Event makeEvent(String event, String domain, String identifier, int version) {
        return new Event(event, domain, identifier, version);
    }

    static MockResponse makeMockStreamResponse(int httpCode, Event... events) {

        final StringBuilder builder = new StringBuilder();
        Arrays.stream(events)
                .forEach(
                        e -> builder.append("event: *\ndata: ").append(new Gson().toJson(e)).append("\n\n"));

        return new MockResponse()
                .setResponseCode(httpCode)
                .setBody(builder.toString())
                .addHeader("Content-Type", "text/event-stream; charset=UTF-8")
                .addHeader("Accept-Encoding", "identity")
                .setSocketPolicy(SocketPolicy.KEEP_OPEN);
    }

    static String makeServerUrl(String host, int port) {
        return  String.format("http://%s:%s/api/1.0", host, port);
    }

    static String makeDummyJwtToken() {
        final String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        final String payload =
                "{\"environment\":\"00000000-0000-0000-0000-000000000000\","
                        + "\"environmentIdentifier\":\"Production\","
                        + "\"project\":\"00000000-0000-0000-0000-000000000000\","
                        + "\"projectIdentifier\":\"dev\","
                        + "\"accountID\":\"aaaaa_BBBBB-cccccccccc\","
                        + "\"organization\":\"00000000-0000-0000-0000-000000000000\","
                        + "\"organizationIdentifier\":\"default\","
                        + "\"clusterIdentifier\":\"1\","
                        + "\"key_type\":\"Server\"}";
        final byte[] hmac256 = new byte[32];
        return Base64.getEncoder().encodeToString(header.getBytes(StandardCharsets.UTF_8))
                + "."
                + Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "."
                + Base64.getEncoder().encodeToString(hmac256);
    }
}
