Harness Feature Flag Android SDK
========================

## Table of Contents
**[Intro](#Intro)**<br>
**[Requirements](#Requirements)**<br>
**[Quickstart](#Quickstart)**<br>
**[Further Reading](docs/further_reading.md)**<br>
**[Build Instructions](docs/build.md)**<br>

## Intro

Harness Feature Flags (FF) is a feature management solution that enables users to change the software’s functionality, 
without deploying new code. FF uses feature flags to hide code or behaviours without having to ship new versions of the software.
A feature flag is like a powerful if statement.
* For more information, see https://harness.io/products/feature-flags/
* To read more, see https://ngdocs.harness.io/category/vjolt35atg-feature-flags
* To sign up, https://app.harness.io/auth/#/signup/

![FeatureFlags](docs/images/ff-gui.png)

## Requirements
[Android Studio](https://developer.android.com/studio?gclid=CjwKCAjwp7eUBhBeEiwAZbHwkRqdhQkk6wroJeWGu0uGWjW9Ue3hFXc4SuB6lwYU4LOZiZ-MQ4p57BoCvF0QAvD_BwE&gclsrc=aw.ds) or the [Android SDK](docs/dev_environment.md) for CLI only<br>
[Java 11](https://www.oracle.com/java/technologies/downloads/#java11) or newer <br>
[Gradle 7.4.1](https://gradle.org/releases/) or newer <br>

## Quickstart
The Feature Flag SDK provides a client that connects to the feature flag service, and fetches the value
of feature flags.  The following section provides an example of how to install the SDK and initalize it from an application.
This quickstart assumes you have followed the instructions to [setup a Feature Flag project and have created a flag called `harnessappdemodarkmode` and created a client API Key](https://ngdocs.harness.io/article/1j7pdkqh7j-create-a-feature-flag#step_1_create_a_project).

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


### A Simple Example
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
If you would like to build, install and run the app from the CLI then follow these steps.
N.B this assumes you have set `$ANDROID_SDK` to the location where the Android SDK has been installed.

#### Setup and Run emulator
If you have already setup an emulator go ahead and start it.  If you haven't setup an emulator you can either use
Android Studio or follow the [CLI steps here](docs/dev_environment.md)
```
$ANDROID_SDK/emulator/emulator @Pixel_4.4_API_32
```

#### Build the project
```shell
cd examples/GettingStarted
./gradlew build
```

#### Install the Project
You must provide the FF_API_KEY which will be compiled in.  You can also optionally override the flag that will be evaluated
by providing FF_FLAG_NAME
```shell
FF_FLAG_NAME="harnessappdemodarkmode" FF_API_KEY="dca85a82-2860-4b12-8bf9-584f3da5ceb8" ./gradlew installDebug
```

The app should show the configured flags current value.  As you toggle the flag in the Harrness UI you will see the
value update.
![Alt Text](docs/images/android_sdk.gif)

### Running with docker
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

### Additional Reading

Further examples and config options are in the further reading section:

[Further Reading](docs/further_reading.md)<br>
[Getting Started Example](examples/GettingStarted)<br>
[Advanced Example](https://github.com/drone/ff-android-client-sample)


-------------------------
[Harness](https://www.harness.io/) is a feature management platform that helps teams to build better software and to
test features quicker.

-------------------------










