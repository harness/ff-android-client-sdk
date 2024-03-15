# Further Reading

Covers advanced topics (different config options and scenarios)

## Client Initialization Options
### Overview
The Harness Feature Flags SDK for Android supports flexible initialization strategies to accommodate different application startup requirements. You can choose between an asynchronous (non-blocking) or synchronous (blocking) approach to initialize the SDK.

### Asynchronous (Non-Blocking) Initialization
To avoid blocking the main thread, the SDK provides asynchronous initialization options. This method allows your application to remain responsive while the SDK initializes in the background.

#### Using callback
Usage:

```Kotlin
val client = CfClient()
client.initialize(this, apiKey, sdkConfiguration, target)
{ info, result ->
    if (result.isSuccess) {
        Log.i("SDKInit", "Successfully initialized client: " + info)
}  else {
        Log.e("SDKInit", "Failed to initialize client", result.error)
    } 
}
```
This approach is non-blocking and leverages a callback mechanism to inform the application about the SDK's initialization status. Here's how it works:
* Success Callback: This callback is invoked once when the SDK is successfully initialized. It signifies that the SDK is ready for use. 

* Failure Callback: This callback may be invoked multiple times if the SDK encounters errors during the initialization process. Each invocation provides an error detailing the cause of failure. The SDK will automatically attempt to retry authentication, leading to multiple invocations of this callback until a successful initialization occurs.

#### Without using a callback
If you don't want to use a callback, you can simply initialize the SDK. Defaults will be served for any variation calls until the SDK can complete initialization.
```Kotlin
val client = CfClient()
client.initialize(this, apiKey, sdkConfiguration, target)
```


### Synchronous (Blocking) Initialization
For scenarios where it's critical to have feature flags loaded and evaluated before proceeding, the SDK offers a blocking initialization method.
This approach ensures that the SDK is fully authenticated and the feature flags are populated from the cache or network before moving forward.

This will block the UI thread, so use synchros initialization only if absolutely required.

Usage:

```kotlin
client.initialize(context, apiKey, configuration, target);
boolean isInitialized = client.waitForInitialization(timeoutMs);
if (isInitialized) {
    Log.i("SDKInit", "Successfully initialized client)
}  else {
    Log.e("SDKInit", "Failed to initialize client")
} 
```


After invoking `initialize(...)`, calling `waitForInitialization(long timeoutMs)` blocks the calling thread until the SDK completes its authentication process or until the specified timeout elapses. A timeoutMs value of 0 waits indefinitely.
Use this method cautiously to prevent blocking the main UI thread, which can lead to a poor user experience.

## Streaming and Polling Mode

By default, Harness Feature Flags SDK has streaming enabled and polling enabled. Both modes can be toggled according to your preference using the SDK's configuration.

### Streaming Mode
Streaming mode establishes a continuous connection between your application and the Feature Flags service.
This allows for real-time updates on feature flags without requiring periodic checks.
If an error occurs while streaming and `pollingEnabled` is set to `true`,
the SDK will automatically fall back to polling mode until streaming can be reestablished.
If `pollingEnabled` is `false`, streaming will attempt to reconnect without falling back to polling.

### Polling Mode
In polling mode, the SDK will periodically check with the Feature Flags service to retrieve updates for feature flags. The frequency of these checks can be adjusted using the SDK's configurations.

### No Streaming or Polling
If both streaming and polling modes are disabled (`enableStream: false` and `enablePolling: false`),
the SDK will not automatically fetch feature flag updates after the initial fetch.
This means that after the initial load, any changes made to the feature flags on the Harness server will not be reflected in the application until the SDK is re-initialized or one of the modes is re-enabled.

This configuration might be useful in specific scenarios where you want to ensure a consistent set of feature flags
for a session or when the application operates in an environment where regular updates are not necessary. However, it's essential to be aware that this configuration can lead to outdated flag evaluations if the flags change on the server.

To configure the modes see [Configuration options](#configuration-options)

## Configuration Options
The following configuration options are available to control the behaviour of the SDK.
You can provide options by adding them to the SDK Configuration.

```Kotlin
        val sdkConfiguration = CfConfiguration.builder()
    .baseUrl("https://config.ff.harness.io/api/1.0")
    .eventUrl("https://events.ff.harness.io/api/1.0")
    .pollingInterval(60)
    .enableStream(true)
    .enablePolling(true)
    .enableAnalytics(true)
    .build()
```


| Name            | Config Option                                    | Description                                                                                                                                      | default                              |
|-----------------|--------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| baseUrl         | baseUrl("https://config.ff.harness.io/api/1.0")  | the URL used to fetch feature flag evaluations. You should change this when using the Feature Flag proxy to http://localhost:7000                | https://config.ff.harness.io/api/1.0 |
| eventsUrl       | eventUrl("https://events.ff.harness.io/api/1.0") | the URL used to post metrics data to the feature flag service. You should change this when using the Feature Flag proxy to http://localhost:7000 | https://events.ff.harness.io/api/1.0 |
| pollInterval    | pollingInterval(60)                              | when running in stream mode, the interval in seconds that we poll for changes.                                                                   | 60                                   |
| enableStream    | enableStream(true)                               | Enable streaming mode.                                                                                                                           | true                                 |
| enableAnalytics | enableAnalytics(true)                            | Enable analytics.  Metrics data is posted every 60s                                                                                              | true                                 |


## Logging Configuration
We use SLF4J. For more information see https://www.slf4j.org/.

To enable it add the following dependencies to your Gradle build file. You can use any framework you prefer, here we are using `logback-android` and `BasicLogcatConfigurator` to bridge SLF4J to Logcat.
```
    implementation 'org.slf4j:slf4j-api:2.0.9'
    implementation 'com.github.tony19:logback-android:3.0.0'
```

Then add a configurator to you app
```
    companion object {
        init {
            BasicLogcatConfigurator.configureDefaultContext()
        }
    }
```

Logs can be defined for classes like so
```
private val log: Logger = LoggerFactory.getLogger(MainActivity::class.java)
...
log.info("my log message")
```

Log items will be visible in Android Studio's Logcat tab for each emulator you are running.

A full example can be found [here](https://github.com/harness/ff-android-client-sdk/blob/main/examples/tlsexample/src/main/java/io/harness/cfsdk/tlsexample/MainActivity.kt)



## Recommended reading

[Feature Flag Concepts](https://ngdocs.harness.io/article/7n9433hkc0-cf-feature-flag-overview)

[Feature Flag SDK Concepts](https://ngdocs.harness.io/article/rvqprvbq8f-client-side-and-server-side-sdks)

## Setting up your Feature Flags

[Feature Flags Getting Started](https://ngdocs.harness.io/article/0a2u2ppp8s-getting-started-with-feature-flags)


## **_SDK Initialization_**
`CfClient` is base class that provides all features of SDK. This is singleton and it is acessed with `CfClient.getInstance()`.

```Kotlin
val sdkConfiguration = CfConfiguration.builder()
    .baseUrl("BASE_API_URL")
    .pollingInterval(60) // Time in seconds
    .enableStream(true)
    .streamUrl("STREAM_URL")
    .build()

val target = Target().identifier("target")

CfClient.getInstance().initialize(context, "YOUR_API_KEY", sdkConfiguration, target)

if (CfClient.getInstance().waitForInitialization(15_000)) {
    // Congratulations your SDK has been initialized with success!
    // After this callback is executed, You are ready to use the SDK!                        
} else {
    // Timeout - check logcat for reason - SDK will attempt to reauthenticate in the background and serve defaults in the mean time
}
```

`target` represents a desired target for which we want features to be evaluated.

`"YOUR_API_KEY"` is a authentication key, needed for access to Harness services.


### **_Public API Methods_** ###
The Public API exposes a few methods that you can utilize:

* `initialize(...)`

```
public void initialize(

        final Context context,
        final String apiKey,
        final CfConfiguration configuration,
        final Target target

) throws IllegalStateException
```

* `public boolean boolVariation(String evaluationId, boolean defaultValue)`

* `public String stringVariation(String evaluationId, String defaultValue)`

* `public double numberVariation(String evaluationId, double defaultValue)`

* `public JSONObject jsonVariation(String evaluationId, JSONObject defaultValue)`

* `public void registerEventsListener(EventsListener listener)`

* `public void unregisterEventsListener(EventsListener observer)`

* `public void refreshEvaluations()`

* `public void close()`

<br><br>

## Fetch evaluation's value
It is possible to fetch a value for a given evaluation. Evaluation is performed based on different type. In case there is no evaluation with provided id, the default value is returned.

Use appropriate method to fetch the desired Evaluation of a certain type.

### <u>_boolVariation(String evaluationId, boolean defaultValue)_</u>

```Kotlin
// Get boolean evaluation:
val evaluation: Boolean = CfClient.getInstance().boolVariation("demo_evaluation", false)  
```

### <u>_numberVariation(String evaluationId, double defaultValue)_</u>

```Kotlin
// Get number evaluation:
val numberEvaluation: Double = CfClient.getInstance().numberVariation("demo_number_evaluation", 0.0)  
```

### <u>_stringVariation(String evaluationId, String defaultValue)_</u>

```Kotlin
// Get String evaluation:
val stringEvaluation: String = CfClient.getInstance().stringVariation("demo_string_evaluation", "demo_value")  
```

### <u>_jsonVariation(String evaluationId, JSONObject defaultValue)_</u>

```Kotlin
// Get JSON evaluation:
val jsonEvaluation: String = CfClient.getInstance().jsonVariation("demo_string_evaluation", JSONObject("{}"))  
```

Note: These methods must not be executed on the application's main thread since they
could trigger the network operations.

## _Register for events_
This method provides a way to register a listener for different events that might be triggered by SDK, indicating specific change in SDK itself.

```Kotlin
private var eventsListener = EventsListener { event ->

    CfLog.OUT.v(tag, "Event: ${event.eventType}")
}

val registerEventsOk = CfClient.getInstance().registerEventsListener(eventsListener)
val unregisterEventsOk = CfClient.getInstance().unregisterEventsListener(eventsListener)
```

## _Unregister from events_
```Kotlin
val success = CfClient.getInstance().unregisterEventsListener(eventsListener)
```

Triggered event will have one of the following types:

```Java
public enum EVENT_TYPE {
    SSE_START,
    SSE_RESUME
    SSE_END,
    EVALUATION_CHANGE,
    EVALUATION_RELOAD
    }
```
Following table provides summary on possible event types and corresponding responses.

| EVENT_TYPE        | Response          |         
|-------------------|:-------:          |      
| SSE_START         | -                 |
| SSE_RESUME        | -                 |
| SSE_END           | -                 |
| EVALUATION_CHANGE | `Evaluation`      | 
| EVALUATION_RELOAD | `List<Evaluation>`|


To avoid unexpected behaviour, when listener is not needed anymore, a caller should call
`CfClient.getInstance().unregisterEventsListener(eventsListener)`. This way the sdk will remove desired listener from internal list.

## _Using feature flags metrics_

Metrics API endpoint can be changed like this:
```kotlin
val remoteConfiguration = CfConfiguration.builder()
    .enableStream(true)
    .pollingInterval(60)
    .enableAnalytics(true)
    .eventUrl(METRICS_API_EVENTS_URL)
    .build()
```

Otherwise, the default metrics endpoint URL will be used.

## _Shutting down the SDK_
To avoid potential memory leak, when SDK is no longer needed (when the app is closed, for example), a caller should call this method:
```Kotlin
CfClient.getInstance().close()
```

## Cloning the SDK repository

In order to clone SDK repository properly perform cloning like in the following example:

```
git clone --recurse-submodules git@github.com:harness/ff-android-client-sdk.git
``` 
