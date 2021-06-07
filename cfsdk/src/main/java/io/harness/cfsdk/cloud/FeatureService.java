package io.harness.cfsdk.cloud;

import java.util.List;

import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;

public interface FeatureService {

    ApiResponse getEvaluations(String target);

    ApiResponse getEvaluationForId(String identifier, String target);



    List<FeatureConfig> getFeatureConfig(

            final String environmentID,
            final String clusterID

    ) throws ApiException;

    FeatureConfig getFeatureConfigByIdentifier(

            String identifier,
            String environmentUUID,
            String clusterIdentifier

    ) throws ApiException;
}
