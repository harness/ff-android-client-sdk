package io.harness.cfsdk.cloud.analytics.model;

import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.EventType;
import io.harness.cfsdk.cloud.model.Target;

public class Analytics {

    private Target target;
    private Variation variation;
    private EventType eventType;
    private FeatureConfig featureConfig;

    public Analytics(

            Target target,
            Variation variation,
            EventType eventType,
            FeatureConfig featureConfig
    ) {

        this.target = target;
        this.variation = variation;
        this.eventType = eventType;
        this.featureConfig = featureConfig;
    }

    public FeatureConfig getFeatureConfig() {
        return featureConfig;
    }

    public void setFeatureConfig(FeatureConfig featureConfig) {

        this.featureConfig = featureConfig;
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
