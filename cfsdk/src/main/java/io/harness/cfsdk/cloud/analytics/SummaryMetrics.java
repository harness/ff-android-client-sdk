package io.harness.cfsdk.cloud.analytics;

public class SummaryMetrics {

    private String featureName;
    private String variationValue;
    private String variationIdentifier;

    public SummaryMetrics(String featureName, String variationValue, String variationIdentifier) {

        this.featureName = featureName;
        this.variationValue = variationValue;
        this.variationIdentifier = variationIdentifier;
    }

    public String getFeatureName() {

        return featureName;
    }

    public void setFeatureName(String featureName) {

        this.featureName = featureName;
    }

    public String getVariationIdentifier() {

        return variationIdentifier;
    }

    public void setVariationIdentifier(String variationIdentifier) {

        this.variationIdentifier = variationIdentifier;
    }

    public String getVariationValue() {

        return variationValue;
    }

    public void setVariationValue(String variationValue) {

        this.variationValue = variationValue;
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
