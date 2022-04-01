package io.harness.cfsdk.cloud.analytics;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;

/**
 * This is a factory class to provide the API for metrics related operations.
 *
 * @author Subir.Adhikari
 * @version 1.0
 * @since 08/01/2021
 */
public class MetricsApiFactory {

    private static MetricsApiFactoryRecipe recipe;

    static {

        recipe = new DefaultMetricsApiFactoryRecipe();
    }

    public static MetricsApi create(

            final String authToken,
            final CfConfiguration config
    ) {

        return recipe.create(authToken, config);
    }

    public static void setDefaultMetricsApiFactoryRecipe(

            final MetricsApiFactoryRecipe metricsApiRecipe
    ) {

        recipe = metricsApiRecipe;
    }
}
