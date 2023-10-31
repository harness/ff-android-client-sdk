package io.harness.cfsdk.logging;

/**
 * SDK logging main entry point.
 */
@Deprecated
public class CfLog {

    /**
     * Logs output strategy.
     */
    public static CfLogging OUT;

    static {

        runtimeModeOn();
    }

    /**
     * Turn on test mode logging strategy.
     * This mode is used by unit tests.
     */
    public static void testModeOn() {

        OUT = new CfLogStrategyTest();
    }

    /**
     * Turn on runtime mode.
     * This mode is used in regular runtime.
     */
    public static void runtimeModeOn() {

        OUT = new CfLogStrategyDefault();
    }

    /**
     * Set the custom logging strategy.
     *
     * @param logging Logging strategy to set.
     */
    public static void customMode(CfLogging logging) {

        OUT = logging;
    }
}
