Harness CF Android SDK
========================
## Overview

-------------------------
[Harness](https://www.harness.io/) is a feature management platform that helps teams to build better software and to test features quicker.

-------------------------

## Setup
The library is packed into `cfsdk-release.aar` file. To use it, create a new module in your project and add this library to it. Next step is adding a dependency to our SDK.
```
//TODO Maven dependency is under development
//implementation 'io.harness:cf-android-client-sdk:1.0.0'

implementation project(path: ':cfsdk')
```
After this step, the SDK elements, primarily `CfClient` should be accessible in main application.

### **_Initialization_**
`CfClient` is base class that provides all features of SDK. This is singleton and it is acessed with `CfClient.getInstance()`. 

``` 
val sdkConfiguration = CfConfiguration.builder()
    .baseUrl("BASE_API_URL")
    .pollingInterval(30) //time in seconds
    .target("your_desired_target")
    .enableStream(true)
    .streamUrl("STREAM_URL")
    .build()

CfClient.getInstance().initialize(context, "YOUR_API_KEY", sdkConfiguration)
```
`target` represents a desired target for which we want features to be evaluated.

`"YOUR_API_KEY"` is a authentication key, needed for access to Harness services.


**Your Harness SDK is now initialized. Congratulations!!!**

<br><br>
### **_Public API Methods_** ###
The Public API exposes a few methods that you can utilize:

* `public void initialize(Context context, String clientId, CfConfiguration configuration, CloudCache cloudCache, AuthCallback authCallback)`

* `public boolean boolEvaluation(String evaluationId, String target, boolean defaultValue)`

* `public String stringEvaluation(String evaluationId, String target, String defaultValue)`

* `public double numberEvaluation(String evaluationId, String target, double defaultValue)`

* `public JSONObject jsonVariation(String evaluationId, String target, JSONObject defaultValue)`

* `public void registerEventsListener(EventsListener listener)`

* `public void unregisterEventsListener(EventsListener observer)`

* `public void destroy()`
<br><br>


#### Fetch evaluation's value
It is possible to fetch a value for a given evaluation. Evaluation is performed based on different type. In case there is no evaluation with provided id, the default value is returned.
```
//get boolean evaluation
val evaluation: Boolean = CfClient.getInstance().boolEvaluation("demo_evaluation", "demo_target", false)  

//get boolean evaluation
val intEvaluation: Double = CfClient.getInstance().numberEvaluation("demo_evaluation", "demo_target", 6 )  
```

#### Register for events
This method provides a way to register a listener for different events that might be triggered by SDK, indicating specific change in SDK itself.

```
private final EventsListener eventsListener = statusEvent -> {
    if (statusEvent.getEventyType() == EVALUATION_CHANGE) {
        Evaluation evaluation = statusEvent.extractPayload();
    }
}

CfClient.getInstance().registerEventsListener(eventsListener)
```


Triggered event will have one of the following types:

```   
public enum EVENT_TYPE {
        SSE_START,
        SSE_END,
        EVALUATION_CHANGE,
        EVALUATION_REMOVE,
        EVALUATION_RELOAD
    }
```
Visit documentation for complete list of possible types and values they provide.

To avoid unexpected behaviour, when listener is not needed anymore, a callar should call 
`CfClient.getInstance().unregisterEventsListener(eventsListener)`
This way the sdk will remove desired listener from internal list.

#### Shutting down the SDK
To avoid potential memory leak, when SDK is no longer needed (when the app is closed, for example), a caller should call this method
```
CfClient.getInstance().destroy()
```
