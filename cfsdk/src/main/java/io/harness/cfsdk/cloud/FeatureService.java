package io.harness.cfsdk.cloud;

public interface FeatureService {
    ApiResponse getEvaluations(String target);

    ApiResponse getEvaluationForId(String identifier, String target);

}
