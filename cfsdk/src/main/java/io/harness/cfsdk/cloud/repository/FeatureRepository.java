package io.harness.cfsdk.cloud.repository;

import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public interface FeatureRepository {

    /*
     * Check cache first for evaluation, if not found then calls getEvaluationFromServer()
     * Returns null if evaluation not found in cache nor on server.
     */
    Evaluation getEvaluation(

            String environment,
            String target,
            String evaluationId,
            String cluster
    );

    /*
     * Bypass the cache and go directly to the ff-server for the evaluation.
     * If network is online and evaluation is found, local cache will be updated
     */
    default Evaluation getEvaluationFromServer(
            String environment,
            String target,
            String evaluationId,
            String cluster

    ) { return null; }

    List<Evaluation> getAllEvaluations(
            String environment,
            String target,
            String cluster
    );

    void remove(String environment, String target, String evaluationId);

    void save(String environment, String target, Evaluation evaluation);

    void clear();
}
