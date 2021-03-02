package io.harness.cfsdk.cloud.events;

import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public interface EvaluationListener {
    void onEvaluation(Evaluation evaluation);

}
