package io.harness.cfsdk;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.events.AuthCallback;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.sse.EventsListener;

public interface Client extends AutoCloseable {

    /**
     * Initialize the SDK.
     * If using this call you should generally call {@link #waitForInitialization(long) waitForInitialization(timeoutMs)} afterwards to block the calling
     * thread to ensure that authentication has completed. Or else you may get default variations served. Alternatively if you don't want to block with
     * waitForInitialization(...) you can use the callback version here {@link CfClient#initialize(Context, String, CfConfiguration, Target, AuthCallback)}
     * @param context The application context, used for any Android-specific initialization (not null).
     * @param apiKey The API key required to authenticate with the service (not null or empty).
     * @param configuration The configuration settings for the SDK, including endpoints and feature flags (not null).
     * @param target The target user or entity for which feature flags and configurations will be evaluated (not null).
     * @throws IllegalStateException if the SDK has already been initialized or if any preconditions are not met (e.g., null parameters).
     */
    void initialize(
            Context context,
            String apiKey,
            CfConfiguration configuration,
            Target target

    ) throws IllegalStateException;

    /**
     * Wait for the SDK to authenticate and populate its cache. This version blocks the calling thread until timeoutMs elapses.
     * Alternatively if you prefer not to block the calling thread you can provide a callback via
     * {@link CfClient#initialize(Context, String, CfConfiguration, Target, AuthCallback)} to be notified when the SDK is ready.
     * @param timeoutMs Timeout in milliseconds to wait for SDK to initialize. Try to avoid setting this to very low values (below
     *                  2 seconds) as network conditions can affect the time it takes to authenticate.
     * @return Returns true if successfully initialized else false if timed out or something went
     * wrong. If false is returned, your code may proceed to use xVariation functions however
     * default variations may be served (SDKCODE 2001). For failure cause check logs leading up.
     * @since 1.2.0
     */
    boolean waitForInitialization(long timeoutMs);

    /**
     * Wait for the SDK to authenticate and populate it cache. This version blocks the calling thread and waits indefinitely, if
     * you require control over startup time then use {@link #waitForInitialization(long) waitForInitialization(timeoutMs)}
     * @throws InterruptedException if the thread was interrupted while waiting
     * @since 1.2.0
     */
    void waitForInitialization() throws InterruptedException;

    /**
     * Retrieve the current state of a boolean feature flag
     * @param evaluationId The identifier of the flag as configured in the UI.
     * @param defaultValue A default value to return if an error is detected or SDK is not
     *                     authenticated.
     * @return The state of the boolean flag.
     */
    boolean boolVariation(String evaluationId, boolean defaultValue);

    /**
     * Retrieve the current state of a string feature flag
     * @param evaluationId The identifier of the flag as configured in the UI.
     * @param defaultValue A default value to return if an error is detected or SDK is not
     *                     authenticated.
     * @return The value of the string flag.
     */
    String stringVariation(String evaluationId, String defaultValue);

    /**
     * Retrieve the current state of a number feature flag
     * @param evaluationId The identifier of the flag as configured in the UI.
     * @param defaultValue A default value to return if an error is detected or SDK is not
     *                     authenticated.
     * @return The value of the number flag.
     */
    double numberVariation(String evaluationId, double defaultValue);

    /**
     * Retrieve the current state of a JSON feature flag
     * @param evaluationId The identifier of the flag as configured in the UI.
     * @param defaultValue A default value to return if an error is detected or SDK is not
     *                     authenticated.
     * @return The value of the JSON flag.
     */
    JSONObject jsonVariation(String evaluationId, JSONObject defaultValue);

    /**
     * Perform a soft-poll. This method will get the latest flag evaluations from the server. It
     * is useful for forcing the SDK's state to refresh when coming to the foreground, after
     * a period of background inactivity where stream events may have been missed.
     * This method will only call out to the server once every minute.
     */
    void refreshEvaluations();

    /**
     * Register a listener to observe changes on a evaluation with given id. The change <strong>will not</strong> be triggered
     * in case of reloading all evaluations, but only when single evaluation is changed.
     * It is possible to register multiple observers for a single evaluatio.
     *
     * @param evaluationId Evaluation identifier we would like to observe.
     * @param listener     {@link EvaluationListener} instance that will be invoked when evaluation is changed
     * @return Was evaluation registered with success?
     */
    boolean registerEvaluationListener(String evaluationId, EvaluationListener listener);

    /**
     * Removes registered listener from list of registered events listener
     *
     * @param observer {@link EventsListener} implementation that needs to be removed
     * @return Was listener un-registered with success?
     */
    boolean unregisterEventsListener(EventsListener observer);

    /**
     * Removes specified listener for an evaluation with given id.
     *
     * @param evaluationId Evaluation identifier.
     * @param listener     {@link EvaluationListener} instance we want to remove
     * @return Was evaluation un-registered with success?
     */
    boolean unregisterEvaluationListener(String evaluationId, EvaluationListener listener);

    /**
     * Adds new listener for various SDK events. See {@link io.harness.cfsdk.cloud.sse.StatusEvent.EVENT_TYPE} for possible types.
     *
     * @param observer {@link EventsListener} implementation that will be triggered when there is a change in state of SDK
     * @return Was listener registered with success?
     */
    boolean registerEventsListener(EventsListener observer);

    /**
     * Clears the occupied resources and shut's down the sdk.
     * After calling this method, the {@link #initialize} must be called again. It will also
     * remove any registered event listeners.
     * @since 1.2.0
     */
    void close();

    /**
     * Deprecated. Use {@link io.harness.cfsdk.CfClient#initialize(Context, String, CfConfiguration, Target)} instead.
     * Kept for source compatibility with 1.x.x projects and will be removed in a future version.
     * Authentication callback has been removed you should instead wait for authentication to complete using {@link CfClient#waitForInitialization()}.
     * If you need to pass in a different cache use  {@link io.harness.cfsdk.CfConfiguration.Builder#cache(CloudCache)} instead of this method.
     */
    @Deprecated
    void initialize(final Context context, final String apiKey, final CfConfiguration config,
                           final Target target, final CloudCache cloudCache, @Nullable final AuthCallback authCallback) throws IllegalStateException;


    /**
     * Initialize the SDK with an {@link io.harness.cfsdk.cloud.events.AuthCallback}.
     * Same as {@link CfClient#initialize(Context, String, CfConfiguration, Target)} but with a callback parameter.
     * You can use this initialize function to provide a lambda that will be called when authentication completes instead
     * of using {@link CfClient#waitForInitialization()}. The callback may be called on the context of the internal SDK thread
     * and you should avoid blocking this for too long and exit the callback quickly.
     * You might get multiple {@link io.harness.cfsdk.cloud.events.AuthResult} (set to false) if there was a failure and the SDK attempts to retry authentication.
     */
    void initialize(final Context context, final String apiKey, final CfConfiguration config,
                           final Target target, final AuthCallback authCallback) throws IllegalStateException;

    /**
     * Deprecated. Use {@link io.harness.cfsdk.CfClient#initialize(Context, String, CfConfiguration, Target)} instead.
     * Kept for source compatibility with 1.x.x projects and will be removed in a future version.
     * If you need to pass in a different cache use  {@link io.harness.cfsdk.CfConfiguration.Builder#cache(CloudCache)} instead of this method.
     */
    @Deprecated
    void initialize(final Context context, final String apiKey, final CfConfiguration config,
                           final Target target, final CloudCache cloudCache) throws IllegalStateException;

}
