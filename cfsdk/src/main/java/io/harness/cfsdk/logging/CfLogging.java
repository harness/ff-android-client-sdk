package io.harness.cfsdk.logging;

/**
 * Log system messages.
 */
public interface CfLogging {

    /**
     * Log verbose.
     * @param tag Log tag.
     * @param message Log message.
     */
    void v(final String tag, final String message);

    /**
     * Log debug.
     * @param tag Log tag.
     * @param message Log message.
     */
    void d(final String tag, final String message);

    /**
     * Log information.
     * @param tag Log tag.
     * @param message Log message.
     */
    void i(final String tag, final String message);

    /**
     * Log warning.
     * @param tag Log tag.
     * @param message Log message.
     */
    void w(final String tag, final String message);

    /**
     * Log error.
     * @param tag Log tag.
     * @param message Log message.
     */
    void e(final String tag, final String message);

    /**
     * Log verbose.
     * @param tag Log tag.
     * @param message Log message.
     * @param throwable Throwable containing stacktrace data.
     */
    void v(final String tag, final String message, final Throwable throwable);

    /**
     * Log debug.
     * @param tag Log tag.
     * @param message Log message.
     * @param throwable Throwable containing stacktrace data.
     */
    void d(final String tag, final String message, final Throwable throwable);

    /**
     * Log info.
     * @param tag Log tag.
     * @param message Log message.
     * @param throwable Throwable containing stacktrace data.
     */
    void i(final String tag, final String message, final Throwable throwable);

    /**
     * Log warning.
     * @param tag Log tag.
     * @param message Log message.
     * @param throwable Throwable containing stacktrace data.
     */
    void w(final String tag, final String message, final Throwable throwable);

    /**
     * Log error.
     * @param tag Log tag.
     * @param message Log message.
     * @param throwable Throwable containing stacktrace data.
     */
    void e(final String tag, final String message, final Throwable throwable);

    /**
     * Log warning.
     * @param tag Log tag.
     * @param throwable Throwable containing stacktrace data.
     */
    void w(final String tag, final Throwable throwable);

    /**
     * Log assertion.
     * @param tag Log tag.
     * @param message Log message.
     */
    void wtf(final String tag, final String message);

    /**
     * Log assertion.
     * @param tag Log tag.
     * @param message Log message.
     * @param throwable Throwable containing stacktrace data.
     */
    void wtf(final String tag, final String message, final Throwable throwable);

    /**
     * Log assertion.
     * @param tag Log tag.
     * @param throwable Throwable containing stacktrace data.
     */
    void wtf(final String tag, final Throwable throwable);
}
