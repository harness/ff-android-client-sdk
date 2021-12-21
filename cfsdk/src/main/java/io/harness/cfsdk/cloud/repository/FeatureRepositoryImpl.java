package io.harness.cfsdk.cloud.repository;

import java.util.Collections;
import java.util.List;

import io.harness.cfsdk.cloud.ApiResponse;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.logging.CfLog;

public class FeatureRepositoryImpl implements FeatureRepository {

    private final String tag;
    private final CloudCache cloudCache;
    private final FeatureService featureService;

    {

        tag = FeatureRepositoryImpl.class.getSimpleName();
    }

    public FeatureRepositoryImpl(FeatureService featureService, CloudCache cloudCache) {

        this.featureService = featureService;
        this.cloudCache = cloudCache;
    }

    @Override
    public Evaluation getEvaluation(

            String environment,
            String target,
            String evaluationId,
            String cluster,
            boolean useCache
    ) {
        if (useCache) {

            return cloudCache.getEvaluation(buildKey(environment, target, evaluationId));
        } else {

            ApiResponse apiResponse = this.featureService.getEvaluationForId(

                    evaluationId, target, cluster
            );
            if (apiResponse != null && apiResponse.isSuccess()) {

                final String key = buildKey(environment, target, evaluationId);
                cloudCache.saveEvaluation(key, apiResponse.body());
                return apiResponse.body();
            }

            return null;
        }
    }

    @Override
    public List<Evaluation> getAllEvaluations(

            String environment, String target, String cluster, boolean fromCache
    ) {

        if (fromCache) {

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

        if (apiResponse != null && !apiResponse.isSuccess()) {

            CfLog.OUT.e(

                    tag,
                    "Get all evaluations API error code: " + apiResponse.getCode()
            );
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
