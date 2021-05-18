package io.harness.cfsdk.cloud.events;

/**
 * Authentication result.
 */
public class AuthResult {

    private boolean success;
    private Throwable error;

    /**
     * Constructor.
     */
    public AuthResult() {
    }

    /**
     * Constructor.
     *
     * @param success True == Authentication is successful.
     */
    public AuthResult(boolean success) {

        this.success = success;
    }

    /**
     * Constructor.
     *
     * @param success True == Authentication is successful.
     * @param error   Throwable containing error message and stacktrace.
     */
    public AuthResult(boolean success, Throwable error) {

        this.success = success;
        this.error = error;
    }

    /**
     * Is authentication successful?
     *
     * @return True == Authentication is successful.
     */
    public boolean isSuccess() {

        return success;
    }

    /**
     * Set authentication success.
     *
     * @param success True == Authentication is successful.
     */
    public void setSuccess(boolean success) {

        this.success = success;
    }

    /**
     * Get authentication error.
     *
     * @return Throwable containing error message and stacktrace.
     */
    public Throwable getError() {

        return error;
    }

    /**
     * Set authentication error.
     *
     * @param error Throwable that contains error message and stacktrace.
     */
    public void setError(Throwable error) {

        this.error = error;
    }
}
