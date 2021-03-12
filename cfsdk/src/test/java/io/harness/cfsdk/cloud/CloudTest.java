package io.harness.cfsdk.cloud;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;

import io.harness.cfsdk.cloud.ApiResponse;
import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.TokenProvider;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.AuthenticationResponse;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.swagger.annotations.Api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class CloudTest {

    @Test
    public void cloudTest() throws ApiException {

        ApiClient apiClient = Mockito.mock(ApiClient.class);
        Mockito.doReturn(apiClient).when(apiClient).addDefaultHeader(any(), any());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        String authToken = "demo_token";
        authenticationResponse.authToken(authToken);

        DefaultApi defaultApi = Mockito.mock(DefaultApi.class);
        Mockito.when(defaultApi.authenticate(any())).thenReturn(authenticationResponse)
        .thenThrow(new ApiException());

        AuthResponseDecoder responseDecoder = Mockito.mock(AuthResponseDecoder.class);
        AuthInfo authInfo = new AuthInfo("", "env", "id","env_id", "", "");
        Mockito.doReturn(authInfo).when(responseDecoder).extractInfo(any());

        TokenProvider tokenProvider = new TokenProvider();

        CloudFactory cloudFactory = Mockito.mock(CloudFactory.class);
        Mockito.when(cloudFactory.getAuthResponseDecoder()).thenReturn(responseDecoder);
        Mockito.when(cloudFactory.apiClient()).thenReturn(apiClient);
        Mockito.when(cloudFactory.defaultApi(any())).thenReturn(defaultApi);
        Mockito.when(cloudFactory.tokenProvider()).thenReturn(tokenProvider);

        Cloud cloud = new Cloud(cloudFactory, "sse_url", "", "demo_key");
        cloud.initialize();

        Mockito.verify(apiClient, Mockito.times(1)).addDefaultHeader(eq("Authorization"), eq("Bearer " + authToken));


        List<Evaluation> evaluationList = new LinkedList<>();
        Evaluation evaluation = new Evaluation();
        evaluation.value = "1";
        evaluation.flag("flag_1");
        evaluationList.add(evaluation);
        Mockito.when(defaultApi.getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target"))).thenReturn(evaluationList);
        Mockito.when(defaultApi.getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target1")))
                .thenThrow(new ApiException(400, "Unauthorized"));

        ApiResponse apiResponse = cloud.getEvaluations("demo_target");

        Mockito.verify(defaultApi, Mockito.times(1)).getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target"));
        Assert.assertEquals(200, apiResponse.getCode());

        ApiResponse errorListResponse = cloud.getEvaluations("demo_target1");
        Assert.assertNull(errorListResponse);

        Mockito.verify(defaultApi, Mockito.times(1)).getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target1"));

        Mockito.when(defaultApi.getEvaluationByIdentifier(eq(authInfo.getEnvironment())
                , eq("flag_1"), eq("demo_target"))
        ).thenReturn(evaluation)
        .thenThrow(new ApiException(400, "Unauthorized"));

        ApiResponse response = cloud.getEvaluationForId("flag_1", "demo_target");
        Mockito.verify(defaultApi, Mockito.times(1)).getEvaluationByIdentifier(eq(authInfo.getEnvironment()), eq("flag_1"),
                eq("demo_target"));
        Assert.assertEquals(response.getCode(), 200);

        ApiResponse errorSingleResponse = cloud.getEvaluationForId("flag_1", "demo_target");
        Assert.assertNull(errorSingleResponse);

        Assert.assertEquals(cloud.getConfig().getAuthentication().getAuthToken(), authToken);

        cloud.initialize();

        Assert.assertEquals(cloud.getConfig().getAuthentication().getAuthToken(), authToken);

    }
}
