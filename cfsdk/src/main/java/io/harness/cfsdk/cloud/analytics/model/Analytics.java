package io.harness.cfsdk.cloud.analytics.model;

import java.util.Objects;

import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;

public class Analytics {

    private final Target target;
    private final String evaluationId;
    private final Variation variation;

    public Analytics(
            Target target,
            String evaluationId,
            Variation variation
    ) {
        this.target = target;
        this.evaluationId = evaluationId;
        this.variation = variation;
    }

    public Target getTarget() {
        return target;
    }

    public Variation getVariation() {
        return variation;
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
