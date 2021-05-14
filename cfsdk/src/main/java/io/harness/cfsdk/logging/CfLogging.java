package io.harness.cfsdk.logging;

public interface CfLogging {

    void v(final String tag, final String message);

    void d(final String tag, final String message);

    void i(final String tag, final String message);

    void w(final String tag, final String message);

    void e(final String tag, final String message);

    void v(final String tag, final String message, final Throwable throwable);

    void d(final String tag, final String message, final Throwable throwable);

    void i(final String tag, final String message, final Throwable throwable);

    void w(final String tag, final String message, final Throwable throwable);

    void e(final String tag, final String message, final Throwable throwable);

    void w(final String tag, final Throwable throwable);

    void wtf(final String tag, final String message);

    void wtf(final String tag, final String message, final Throwable throwable);

    void wtf(final String tag, final Throwable throwable);
}
