package io.harness.cfsdk.cloud.analytics.model;

import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.EventType;
import io.harness.cfsdk.cloud.model.Target;

public class Analytics {

    private FeatureConfig featureConfig;
    private Target target;
    private Variation variation;
    private EventType eventType = EventType.METRICS;
}
