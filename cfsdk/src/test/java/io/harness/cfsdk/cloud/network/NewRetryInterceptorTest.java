package io.harness.cfsdk.cloud.network;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class NewRetryInterceptorTest {

    @Test
    public void shouldParseRetryHeader() {
        final NewRetryInterceptor interceptor = new NewRetryInterceptor(0);
        final Request dummyReq = new Request.Builder().url("http://test.com").build();
        assertEquals(0, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(100).addHeader("Retry-After", "1").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(0, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(200).addHeader("Retry-After", "1").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(0, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(429).request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(0, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(503).request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(111, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(301).addHeader("Retry-After", "111").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(321, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(429).addHeader("Retry-After", "321").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(123, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(503).addHeader("Retry-After", "123").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(3600, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(503).addHeader("Retry-After", "999999999").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(3600, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(503).addHeader("Retry-After", "Sat, 1 Jan 2050 00:00:00 GMT").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
        assertEquals(0, interceptor.getRetryAfterHeaderInSeconds( new Response.Builder().code(503).addHeader("Retry-After", "Mon, 1 Jan 1990 00:00:00 GMT").request(dummyReq).protocol(Protocol.HTTP_2).message("").build()));
    }
}
