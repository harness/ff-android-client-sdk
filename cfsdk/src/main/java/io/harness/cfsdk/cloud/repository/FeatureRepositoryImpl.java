package io.harness.cfsdk.cloud.repository;

import java.util.Collections;
import java.util.List;

import io.harness.cfsdk.cloud.ApiResponse;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.logging.CfLog;

public class FeatureRepositoryImpl implements FeatureRepository {

    private final String tag;
    private final CloudCache cloudCache;
    private final FeatureService featureService;
    private final NetworkInfoProviding networkInfoProvider;

    {

        tag = FeatureRepositoryImpl.class.getSimpleName();
    }

    public FeatureRepositoryImpl(

            final FeatureService featureService,
            final CloudCache cloudCache,
            final NetworkInfoProviding networkInfoProvider
    ) {

        this.cloudCache = cloudCache;
        this.featureService = featureService;
        this.networkInfoProvider = networkInfoProvider;
    }

    @Override
    public Evaluation getEvaluation(

            String environment,
            String target,
            String evaluationId,
            String cluster
    ) {
        if (networkInfoProvider.isNetworkAvailable()) {

            ApiResponse apiResponse = this.featureService.getEvaluationForId(

                    evaluationId, target, cluster
            );

            if (apiResponse != null && apiResponse.isSuccess()) {

                final String key = buildKey(environment, target, evaluationId);
                cloudCache.saveEvaluation(key, apiResponse.body());
                return apiResponse.body();
            }
        }

        return cloudCache.getEvaluation(buildKey(environment, target, evaluationId));
    }

    @Override
    public List<Evaluation> getAllEvaluations(

            String environment, String target, String cluster
    ) {

        if (!networkInfoProvider.isNetworkAvailable()) {

            return this.cloudCache.getAllEvaluations(environment + "_" + target);
        }

        final ApiResponse apiResponse = this.featureService.getEvaluations(target, cluster);
        if (apiResponse != null && apiResponse.isSuccess()) {

            List<Evaluation> evaluationList = apiResponse.body();
            for (Evaluation evaluation : evaluationList) {

                cloudCache.saveEvaluation(

                        buildKey(environment, target, evaluation.getFlag()), evaluation
                );
            }

            CfLog.OUT.v(tag, "Got all evaluations: " + evaluationList.size());
            return evaluationList;
        }

        if (apiResponse == null || !apiResponse.isSuccess()) {

            if (apiResponse!=null) {

                CfLog.OUT.e(

                        tag,
                        "Get all evaluations, API error code: " + apiResponse.getCode()
                );

            } else {

                CfLog.OUT.e(

                        tag,
                        "Get all evaluations, got null API response"
                );
            }

            return this.cloudCache.getAllEvaluations(environment + "_" + target);
        }

        CfLog.OUT.w(tag, "Got no evaluations");
        return Collections.emptyList();

    }

    @Override
    public void remove(String environment, String target, String evaluationId) {

        this.cloudCache.removeEvaluation(buildKey(environment, target, evaluationId));
    }


    @Override
    public void clear() {

        cloudCache.clear();
    }

    private String buildKey(String environment, String target, String featureId) {

        return environment + "_" + target + "_" + featureId;
    }

}
