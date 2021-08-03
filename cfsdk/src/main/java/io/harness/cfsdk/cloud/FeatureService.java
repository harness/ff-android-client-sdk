package io.harness.cfsdk.cloud;

public interface FeatureService {

    ApiResponse getEvaluations(String target, String cluster);

    ApiResponse getEvaluationForId(String identifier, String target, String cluster);
}
