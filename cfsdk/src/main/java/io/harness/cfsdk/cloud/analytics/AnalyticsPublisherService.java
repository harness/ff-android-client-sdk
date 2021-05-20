package io.harness.cfsdk.cloud.analytics;


import java.util.HashSet;
import java.util.Set;

import io.harness.cfsdk.cloud.model.Target;

/**
 * This class prepares the message body for metrics and posts it to the server
 */
public class AnalyticsPublisherService {

    private static final String FEATURE_NAME_ATTRIBUTE;
    private static final String FEATURE_VALUE_ATTRIBUTE;
    private static final String VARIATION_VALUE_ATTRIBUTE;
    private static final String VARIATION_IDENTIFIER_ATTRIBUTE;
    private static final String TARGET_ATTRIBUTE;
    private static final Set<Target> globalTargetSet;
    private static final Set<Target> stagingTargetSet;
    private static final String JAR_VERSION;
    private static final String SDK_TYPE;
    private static final String ANONYMOUS_TARGET;
    private static final String SERVER;
    private static final String SDK_LANGUAGE;
    private static final String SDK_VERSION;

    static {

        SERVER = "server";
        SDK_TYPE = "SDK_TYPE";
        JAR_VERSION = "JAR_VERSION";
        TARGET_ATTRIBUTE = "target";
        SDK_VERSION = "SDK_VERSION";
        SDK_LANGUAGE = "SDK_LANGUAGE";
        ANONYMOUS_TARGET = "anonymous";
        globalTargetSet = new HashSet<>();
        stagingTargetSet = new HashSet<>();
        FEATURE_NAME_ATTRIBUTE = "featureName";
        FEATURE_VALUE_ATTRIBUTE = "featureValue";
        VARIATION_VALUE_ATTRIBUTE = "featureValue";
        VARIATION_IDENTIFIER_ATTRIBUTE = "variationIdentifier";
    }

    private String jarVerion = "";
}
