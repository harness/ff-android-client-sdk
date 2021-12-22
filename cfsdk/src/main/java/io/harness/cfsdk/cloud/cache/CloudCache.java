package io.harness.cfsdk.cloud.cache;

import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public interface CloudCache {

    Evaluation getEvaluation(String key);

    void saveEvaluation(String key, Evaluation evaluation);

    List<Evaluation> getAllEvaluations(String key);

    void removeEvaluation(String key);

    void clear();
}
