package io.harness.cfsdk.cloud.events;

import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

public interface EvaluationListener {

    void onEvaluation(Evaluation evaluation);
}
