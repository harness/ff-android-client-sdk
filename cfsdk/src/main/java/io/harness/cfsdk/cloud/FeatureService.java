package io.harness.cfsdk.cloud;

import java.util.List;

import io.harness.cfsdk.cloud.core.client.ApiException;

public interface FeatureService {

    ApiResponse getEvaluations(String target, String cluster);

    ApiResponse getEvaluationForId(String identifier, String target, String cluster);
}
