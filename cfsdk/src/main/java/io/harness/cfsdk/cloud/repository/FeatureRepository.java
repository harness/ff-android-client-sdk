package io.harness.cfsdk.cloud.repository;

import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public interface FeatureRepository {

    Evaluation getEvaluation(

            String environment,
            String target,
            String evaluationId,
            String cluster
    );

    List<Evaluation> getAllEvaluations(

            String environment,
            String target,
            String cluster
    );

    void remove(String environment, String target, String evaluationId);

    void clear();
}
