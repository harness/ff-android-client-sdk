package io.harness.cfsdk.cloud.cache;

import java.util.List;

import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

public interface CloudCache {

    Evaluation getEvaluation(String env, String key);

    void saveEvaluation(String env, String key, Evaluation evaluation);

    void removeEvaluation(String env, String key);

    List<Evaluation> getAllEvaluations(String env);

    void saveAllEvaluations(String env, List<Evaluation> evaluations);

    void clear();
}
