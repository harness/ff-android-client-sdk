Android SDK for Harness Feature Flags
========================

## Table of Contents
**[Intro](#Intro)**<br>
**[Requirements](#Requirements)**<br>
**[Quickstart](#Quickstart)**<br>
**[Further Reading](docs/further_reading.md)**<br>
**[Build Instructions](docs/build.md)**<br>

## Intro

Use this README to get started with our Feature Flags (FF) SDK for Android. This guide outlines the basics of getting started with the SDK and provides a full code sample for you to try out.

This sample doesn’t include configuration options, for in depth steps and configuring the SDK, for example, disabling streaming or using our Relay Proxy, see the [Android SDK Reference](https://ngdocs.harness.io/article/74t18egxbi-android-sdk-reference).

For a sample FF Android SDK project, see [our test Android Project](https://github.com/harness/ff-android-client-sdk/tree/main/examples/GettingStarted).


![FeatureFlags](docs/images/ff-gui.png)

## Requirements

To use this SDK, make sure you've:

- [Android Studio](https://developer.android.com/studio?gclid=CjwKCAjwp7eUBhBeEiwAZbHwkRqdhQkk6wroJeWGu0uGWjW9Ue3hFXc4SuB6lwYU4LOZiZ-MQ4p57BoCvF0QAvD_BwE&gclsrc=aw.ds) or the [Android SDK](docs/dev_environment.md) for CLI only<br>
- [Java 11](https://www.oracle.com/java/technologies/downloads/#java11) or newer <br>
- [Gradle 7.4.1](https://gradle.org/releases/) or newer <br>

To follow along with our test code sample, make sure you’ve:
- [Created a Feature Flag](https://ngdocs.harness.io/article/1j7pdkqh7j-create-a-feature-flag) on the Harness Platform called harnessappdemodarkmode
- Created a [server/client SDK key](https://ngdocs.harness.io/article/1j7pdkqh7j-create-a-feature-flag#step_3_create_an_sdk_key) and made a copy of it

### Install the SDK
You can add the Android SDK to your application by adding the following snippet to root project's [build.gradle](https://github.com/harness/ff-android-client-sdk/blob/main/examples/GettingStarted/build.gradle#L2) file:
```gradle
buildscript {
    repositories {
        mavenCentral()
    }
}
```

In app module's [build.gradle](https://github.com/harness/ff-android-client-sdk/blob/main/examples/GettingStarted/app/build.gradle#L41) file add dependency for Harness's SDK

`implementation 'io.harness:ff-android-client-sdk:1.0.9'`


### Code Sample
Here is a complete [example](https://github.com/harness/ff-android-client-sdk/blob/main/examples/GettingStarted/app/src/main/java/io/harness/cfsdk/gettingstarted/MainActivity.kt) that will connect to the feature flag service and report the flag value.  An event listener is registered
to receive flag change events.
Any time a flag is toggled from the feature flag service you will receive the updated value.

```Kotlin
package io.harness.cfsdk.gettingstarted

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import io.harness.cfsdk.*
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.Target

class MainActivity : AppCompatActivity() {

    private var flagName: String = BuildConfig.FF_FLAG_NAME.ifEmpty { "harnessappdemodarkmode" }

    // The SDK API Key to use for authentication.  Configure it when installing the app by setting FF_API_KEY
    // e.g. FF_API_KEY='my key' ./gradlew installDebug
    private val apiKey: String = BuildConfig.FF_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        printMessage("Starting SDK")

        if (flagName.equals("null")) { flagName = "harnessappdemodarkmode" }

        // Create Default Configuration for the SDK.  We can use this to disable streaming,
        // change the URL the client connects to etc
        val sdkConfiguration = CfConfiguration.builder().enableStream(true).build()

        // Create a target (different targets can get different results based on rules.  This include a custom attribute 'location')
        val target = Target().identifier("ff-android").name("FF Android")
        target.attributes["location"] = "emea"

        // Init the default instance of the Feature Flag Client
        CfClient.getInstance().initialize(this, apiKey, sdkConfiguration, target)
        { info, result ->
            if (result.isSuccess) {
                Log.i("SDKInit", "Successfully initialized client")

                // Get initial value of flag and display it
                var flagValue : Boolean = CfClient.getInstance().boolVariation(flagName, false)
                printMessage("$flagName : $flagValue")

                // Setup Listener to handle flag change events.  This fires when a flag is modified
                CfClient.getInstance().registerEvaluationListener(flagName, EvaluationListener {
                    Log.i("SDKEvent", "received event for flag")
                    var flagValue : Boolean = CfClient.getInstance().boolVariation(flagName, false)
                    printMessage("$flagName : $flagValue")
               })
            } else {
                Log.e("SDKInit", "Failed to initialize client", result.error)
                result.error.message?.let { printMessage(it) }
            }
        }
    }

    // printMessage uses the UI Thread to update the text on the display
    private fun printMessage(msg : String) {
        val tv1: TextView = findViewById(R.id.textView1)
        runOnUiThread { tv1.text = msg }
    }
}
```

### Running the example
If you want to run the [getting started example](examples/GettingStarted), then you can open the project in Android Studio.
If you would like to build, install and run the app from the CLI then follow these [steps to setup the SDK](docs/dev_environment.md).

N.B this assumes you have set `$ANDROID_SDK` to the location where the Android SDK has been installed.

<br>

#### Start the emulator
```
$ANDROID_SDK/emulator/emulator @Pixel_4.4_API_32
```
<br>

#### Build the project
```shell
cd examples/GettingStarted
./gradlew build
```
<br>

#### Install the Project
You must provide the FF_API_KEY which will be compiled in.
You can also optionally override the flag that will be evaluated
by providing FF_FLAG_NAME
```shell
FF_FLAG_NAME="harnessappdemodarkmode" FF_API_KEY="dca85a82-2860-4b12-8bf9-584f3da5ceb8" ./gradlew installDebug
```
<br>
The app should show the configured flags current value.  As you toggle the flag in the Harrness UI you will see the
value update.
<br><br>

![Alt Text](docs/images/android_sdk.gif)

<br>

### Running the example with docker
You will need to install the Android SDK in order to run the emulator, but if you wish to avoid installing Java, Gradle etc
you can use a docker image to compile and install the application to a locally running emulator.
Follow the steps to [setup and run the emulator](#Setup-and-Run-emulator).

With the emulator running build and install the app
```shell
# Build the code
docker run -v $(pwd):/app -v "$HOME/.dockercache/gradle":"/root/.gradle" -w /app mingc/android-build-box ./gradlew build

# Install Debug build to emulator
docker run -v $(pwd):/app -v "$HOME/.dockercache/gradle":"/root/.gradle" -w /app mingc/android-build-box ./gradlew installDebug
```
<br>

### Additional Reading

Further examples and config options are in the further reading section:

[Further Reading](docs/further_reading.md)<br>
[Getting Started Example](examples/GettingStarted)<br>
[Advanced Example](https://github.com/drone/ff-android-client-sample)


-------------------------
[Harness](https://www.harness.io/) is a feature management platform that helps teams to build better software and to
test features quicker.

-------------------------










