package io.harness.cfsdk.logging;

/**
 * SDK logging main entry point.
 */
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
}
