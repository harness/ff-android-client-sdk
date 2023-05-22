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

            final String environment,
            final String target,
            final String evaluationId,
            final String cluster
    ) {
        Evaluation eval = cloudCache.getEvaluation(buildKey(environment, target), evaluationId);

        if (eval == null) {
            eval = getEvaluationFromServer(environment, target, evaluationId, cluster);
        }

        return eval;
    }

    @Override
    public Evaluation getEvaluationFromServer(
        final String environment,
        final String target,
        final String evaluationId,
        final String cluster) {

        if (networkInfoProvider.isNetworkAvailable()) {
            // Not in the cache, call out to the network for it

            ApiResponse apiResponse = this.featureService.getEvaluationForId(
                    evaluationId, target, cluster
            );

            if (apiResponse != null && apiResponse.isSuccess()) {

                final String env = buildKey(environment, target);
                cloudCache.saveEvaluation(env, evaluationId, apiResponse.body());
                return apiResponse.body();
            }
        }

        return null;
    }

    @Override
    public List<Evaluation> getAllEvaluations(

            String environment, String target, String cluster
    ) {

        final String envKey = environment + "_" + target;

        CfLog.OUT.v(tag, "getAllEvaluations(): " + envKey);

        if (!networkInfoProvider.isNetworkAvailable()) {

            CfLog.OUT.v(tag, "getAllEvaluations(), returning from the cache: " + envKey);

            return this.cloudCache.getAllEvaluations(envKey);
        }

        final ApiResponse apiResponse = this.featureService.getEvaluations(target, cluster);
        if (apiResponse != null && apiResponse.isSuccess()) {

            final List<Evaluation> evaluationList = apiResponse.body();
            cloudCache.saveAllEvaluations(envKey, evaluationList);

            CfLog.OUT.v(

                    tag,
                    "getAllEvaluations(), returning from the cloud, size: " +
                            evaluationList.size()
            );

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

        this.cloudCache.removeEvaluation(buildKey(environment, target), evaluationId);
    }

    @Override
    public void save(String environment, String target, Evaluation evaluation) {
        final String env = buildKey(environment, target);
        cloudCache.saveEvaluation(env, evaluation.getFlag(), evaluation);
    }


    @Override
    public void clear() {

        cloudCache.clear();
    }

    private String buildKey(String environment, String target) {

        return environment + "_" + target;
    }

}
