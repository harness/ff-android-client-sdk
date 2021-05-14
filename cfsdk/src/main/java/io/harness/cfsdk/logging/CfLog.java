package io.harness.cfsdk.logging;

public class CfLog {

    public static CfLogging OUT;

    static {

        runtimeModeOn();
    }

    public static void testModeOn() {

        OUT = new CfLogStrategyTest();
    }

    public static void runtimeModeOn() {

        OUT = new CfLogStrategyDefault();
    }
}
