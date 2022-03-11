Harness Feature Flag Android SDK
========================

## Table of Contents
**[Intro](#Intro)**<br>
**[Requirements](#Requirements)**<br>
**[Quickstart](#Quickstart)**<br>
**[Further Reading](docs/further_reading.md)**<br>
**[Build Instructions](docs/build.md)**<br>


## Intro

Harness Feature Flags (FF) is a feature management solution that enables users to change the software’s functionality, without deploying new code. FF uses feature flags to hide code or behaviours without having to ship new versions of the software. A feature flag is like a powerful if statement.
* For more information, see https://harness.io/products/feature-flags/
* To read more, see https://ngdocs.harness.io/category/vjolt35atg-feature-flags
* To sign up, https://app.harness.io/auth/#/signup/

![FeatureFlags](https://github.com/harness/ff-python-server-sdk/raw/main/docs/images/ff-gui.png)

## Requirements

[Java 11](https://www.oracle.com/java/technologies/downloads/#java11)<br>
[Gradle 7](https://docs.gradle.org/current/userguide/installation.html)<br>
[Android SDK](https://developer.android.com/studio#downloads)<br>
<br>

### Git Submodule Init
We need to init a submodule that contains some shared logging code
```bash
git submodule init 
git submodule update
```

## Quickstart
The Feature Flag SDK provides a client that connects to the feature flag service, and fetches the value
of featue flags.   The following section provides an example of how to install the SDK and initalize it from
an application.
This quickstart assumes you have followed the instructions to [setup a Feature Flag project and have created a flag called `simpleboolflag` and created a server API Key](https://ngdocs.harness.io/article/1j7pdkqh7j-create-a-feature-flag#step_1_create_a_project).


## Setup

Add following snippet to root project's `build.gradle` file:
```
buildscript {
    repositories {
        mavenCentral()
    }
```

In app module's `build.gradle` file add dependency for Harness's SDK
`implementation 'io.harness:ff-android-client-sdk:1.0.7'`

After this step, the SDK elements, primarily `CfClient` should be accessible in main application.

## **_Initialization_**
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
{ info, result ->

    if (result.isSuccess) {
        
        // Congratulations your SDK has been initialized with success!
        // After this callback is executed, You are ready to use the SDK!                        
    }
}
```

`target` represents a desired target for which we want features to be evaluated.

`"YOUR_API_KEY"` is a authentication key, needed for access to Harness services.


**Your Harness SDK is now initialized. Congratulations!!!**

<br><br>
### **_Public API Methods_** ###
The Public API exposes a few methods that you can utilize:

* `public void initialize(Context context, String clientId, CfConfiguration configuration, CloudCache cloudCache, AuthCallback authCallback)`

* `public boolean boolVariation(String evaluationId, boolean defaultValue)`

* `public String stringVariation(String evaluationId, String defaultValue)`

* `public double numberVariation(String evaluationId, double defaultValue)`

* `public JSONObject jsonVariation(String evaluationId, JSONObject defaultValue)`

* `public void registerEventsListener(EventsListener listener)`

* `public void unregisterEventsListener(EventsListener observer)`

* `public void destroy()`

<br><br>


## Fetch evaluation's value
It is possible to fetch a value for a given evaluation. Evaluation is performed based on different type. In case there is no evaluation with provided id, the default value is returned.

Use appropriate method to fetch the desired Evaluation of a certain type.

### <u>_boolVariation(String evaluationId, boolean defaultValue)_</u>
```Kotlin
//get boolean evaluation
val evaluation: Boolean = CfClient.getInstance().boolVariation("demo_evaluation", false)  
```
### <u>_numberVariation(String evaluationId, double defaultValue)_</u>
```Kotlin
//get number evaluation
val numberEvaluation: Double = CfClient.getInstance().numberVariation("demo_number_evaluation", 0.0)  
```

### <u>_stringVariation(String evaluationId, String defaultValue)_</u>
```Kotlin
//get String evaluation
val stringEvaluation: String = CfClient.getInstance().stringVariation("demo_string_evaluation", "demo_value")  
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
```

## _Unregister from events_
```Kotlin
val success = CfClient.getInstance().unregisterEventsListener(eventsListener)
```

Triggered event will have one of the following types:

```Java
public enum EVENT_TYPE {
        SSE_START, 
        SSE_END, 
        EVALUATION_CHANGE,
        EVALUATION_RELOAD
    }
```
Following table provides summary on possible event types and corresponding responses.

| EVENT_TYPE        | Response          |         
| -------------     |:-------:          |      
| SSE_START         | -                 |
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
To avoid potential memory leak, when SDK is no longer needed (when the app is closed, for example), a caller should call this method
```Kotlin
CfClient.getInstance().destroy()
```

## Cloning the SDK repository

In order to clone SDK repository properly perform cloning like in the following example:

```
git clone --recurse-submodules git@github.com:harness/ff-android-client-sdk.git
``` 

## Using SDK in unit tests

To be able to use the SDK in unit tests it is required to set SDKs logging to the console output:

```Kotlin
CfLog.testModeOn()
```

`testModeOn` will turn on the use of the system output logging strategy. 

On the other hand, to turn on the usage of the Android [log class](https://developer.android.com/reference/android/util/Log) use:

```Kotlin
CfLog.runtimeModeOn()
``` 

Standard Android logging is the default logging strategy so turning on runtime mode is not required. 

-------------------------
[Harness](https://www.harness.io/) is a feature management platform that helps teams to build better software and to test features quicker.
-------------------------
