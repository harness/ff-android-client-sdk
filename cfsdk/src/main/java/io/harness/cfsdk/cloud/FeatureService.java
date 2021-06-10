package io.harness.cfsdk.cloud;

import java.util.List;

import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;

public interface FeatureService {

    ApiResponse getEvaluations(String target, String clusterIdentifier);

    ApiResponse getEvaluationForId(String identifier, String target, String clusterIdentifier);

    List<FeatureConfig> getFeatureConfig(

            final String environmentID,
            final String clusterIdentifier

    ) throws ApiException;

    FeatureConfig getFeatureConfigByIdentifier(

            String identifier,
            String environmentUUID,
            String clusterIdentifier

    ) throws ApiException;
}
