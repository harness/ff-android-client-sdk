package io.harness.cfsdk.cloud.core.client;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private final long maxTryCount;
    private final long retryBackoffDelay;

    public RetryInterceptor(long maxTryCount, long retryBackoffDelay) {
        this.maxTryCount = maxTryCount;
        this.retryBackoffDelay = retryBackoffDelay;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        int tryCount = 0;
        while (!response.isSuccessful() && tryCount < maxTryCount) {
            log.debug("Request is not successful - {}", tryCount);

            response.close();

            tryCount++;

            try {
                Thread.sleep(retryBackoffDelay * tryCount);
            } catch (InterruptedException e) {
                log.debug("Request is not successful - {}", tryCount, e);
            }
            // retry the request
            response = chain.proceed(request);
        }
        return response;
    }
    }
}
