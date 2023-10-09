package io.harness.cfsdk.cloud.analytics;

public class SummaryMetrics {

    private final String featureName;
    private final String variationValue;
    private final String variationIdentifier;
    private final String target;

    public SummaryMetrics(String featureName, String variationValue, String variationIdentifier, String target) {

        this.featureName = featureName;
        this.variationValue = variationValue;
        this.variationIdentifier = variationIdentifier;
        this.target = target;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getVariationIdentifier() {
        return variationIdentifier;
    }

    public String getVariationValue() {
        return variationValue;
    }

    public String getTarget() {
        return target;
    }


    @Override
    public String toString() {

        return "SummaryMetrics{" +
                "featureName='" + featureName + '\'' +
                ", variationValue='" + variationValue + '\'' +
                ", variationIdentifier='" + variationIdentifier + '\'' +
                '}';
    }
}
