package io.harness.cfsdk.cloud.analytics.model;

import java.util.Objects;

import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;

public class Analytics {

    private Target target;
    private String evaluationId;
    private Variation variation;

    public Analytics(

            Target target,
            String evaluationId,
            Variation variation
    ) {

        this.target = target;
        this.evaluationId = evaluationId;
        this.variation = variation;
    }

    public String getEvaluationId() {

        return evaluationId;
    }

    public void setEvaluationId(String evaluationId) {

        this.evaluationId = evaluationId;
    }

    public Target getTarget() {

        return target;
    }

    public void setTarget(Target target) {

        this.target = target;
    }

    public Variation getVariation() {

        return variation;
    }

    public void setVariation(Variation variation) {

        this.variation = variation;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {

            return true;
        }
        if (o == null || getClass() != o.getClass()) {

            return false;
        }

        final Analytics analytics = (Analytics) o;

        return Objects.equals(target.getIdentifier(), analytics.target.getIdentifier()) &&
                Objects.equals(evaluationId, analytics.evaluationId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(target.getIdentifier(), evaluationId);
    }
}
