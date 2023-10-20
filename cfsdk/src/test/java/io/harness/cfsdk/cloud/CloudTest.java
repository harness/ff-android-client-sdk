package io.harness.cfsdk.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import io.harness.cfsdk.cloud.openapi.client.ApiClient;
import io.harness.cfsdk.cloud.openapi.client.ApiException;
import io.harness.cfsdk.cloud.openapi.client.api.ClientApi;
import io.harness.cfsdk.cloud.openapi.client.model.AuthenticationResponse;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;

public class CloudTest {

    @Test
    public void cloudTest() throws ApiException {

        ApiClient apiClient = mock(ApiClient.class);
        doReturn(apiClient).when(apiClient).addDefaultHeader(any(), any());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        String authToken = "demo_token";
        authenticationResponse.authToken(authToken);

        ClientApi defaultApi = mock(ClientApi.class);
        when(defaultApi.authenticate(any())).thenReturn(authenticationResponse)
                .thenThrow(new ApiException());

        AuthResponseDecoder responseDecoder = mock(AuthResponseDecoder.class);
        AuthInfo authInfo = new AuthInfo("", "env", "id", "env_id", "", "", "");
        doReturn(authInfo).when(responseDecoder).extractInfo(any());

        TokenProvider tokenProvider = new TokenProvider();

        CloudFactory cloudFactory = mock(CloudFactory.class);

        when(cloudFactory.getAuthResponseDecoder()).thenReturn(responseDecoder);
        when(cloudFactory.apiClient()).thenReturn(apiClient);
        when(cloudFactory.defaultApi(any())).thenReturn(defaultApi);
        when(cloudFactory.tokenProvider()).thenReturn(tokenProvider);
        Target target = new Target();
        target.identifier("Fiserv");
        Cloud cloud = new Cloud(cloudFactory, "sse_url", "", "demo_key", target, null);
        cloud.initialize();

        verify(apiClient, times(1)).addDefaultHeader(eq("Authorization"), eq("Bearer " + authToken));

        List<Evaluation> evaluationList = new LinkedList<>();
        Evaluation evaluation = new Evaluation();
        evaluation.setValue("1");
        evaluation.flag("flag_1");
        evaluationList.add(evaluation);
        when(defaultApi.getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target"), anyString())).thenReturn(evaluationList);
        when(defaultApi.getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target1"), anyString()))
                .thenThrow(new ApiException(400, "Unauthorized"));

        ApiResponse apiResponse = cloud.getEvaluations("demo_target", "");

        verify(defaultApi, times(1)).getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target"), eq(""));
        assertEquals(200, apiResponse.getCode());

        ApiResponse errorListResponse = cloud.getEvaluations("demo_target1", "");
        assertNull(errorListResponse);

        verify(defaultApi, times(1)).getEvaluations(eq(authInfo.getEnvironment()), eq("demo_target1"), eq(""));

        when(defaultApi.getEvaluationByIdentifier(eq(authInfo.getEnvironment())
                        , eq("flag_1"), eq("demo_target"), eq(""))
                ).thenReturn(evaluation)
                .thenThrow(new ApiException(400, "Unauthorized"));

        ApiResponse response = cloud.getEvaluationForId("flag_1", "demo_target", "");
        verify(defaultApi, times(1)).getEvaluationByIdentifier(eq(authInfo.getEnvironment()), eq("flag_1"),
                eq("demo_target"), anyString());
        assertEquals(response.getCode(), 200);

        ApiResponse errorSingleResponse = cloud.getEvaluationForId("flag_1", "demo_target", "");
        assertNull(errorSingleResponse);

        assertEquals(cloud.getAuthToken(), authToken);

        assertThrows(ApiException.class,
                cloud::initialize);

        assertEquals(cloud.getAuthToken(), authToken);
    }
}
