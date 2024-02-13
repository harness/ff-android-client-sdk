package io.harness.cfsdk.gettingstarted

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.qos.logback.classic.android.BasicLogcatConfigurator
import io.harness.cfsdk.*
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.sse.StatusEvent


class MainActivity : AppCompatActivity() {

    companion object {
        init {
            BasicLogcatConfigurator.configureDefaultContext() // enable SDK logging to logcat
        }
    }

    private var flagName: String = BuildConfig.FF_FLAG_NAME.ifEmpty { "harnessappdemodarkmode" }

    // The SDK API Key to use for authentication.  Configure it when installing the app by setting FF_API_KEY
    // e.g. FF_API_KEY='my key' ./gradlew installDebug
    private val apiKey: String = BuildConfig.FF_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        printMessage("Starting SDK")


        if (flagName.equals("null")) {
            flagName = "harnessappdemodarkmode"
        }

        // Create Default Configuration for the SDK.  We can use this to disable streaming,
        // change the URL the client connects to etc
        val sdkConfiguration = CfConfiguration.builder().enableStream(true).build()

        // Create a target (different targets can get different results based on rules.  This include a custom attribute 'location')
        val target = Target().identifier("ff-android").name("FF Android")
        target.attributes["location"] = "emea"

        // Init the default instance of the Feature Flag Client

        val client = CfClient()
        client.use {
            client.initialize(this, apiKey, sdkConfiguration, target)
            if (client.waitForInitialization(60_000)) {
                Log.i("SDKInit", "Successfully initialized client")

                // Get initial value of flag and display it
                var flagValue: Boolean = client.boolVariation(flagName, false)
                printMessage("$flagName : $flagValue")

                // Setup Listener to handle different events emitted by the SDK
                client.registerEventsListener { event ->
                    when (event.eventType) {
                        // Setup Listener to handle flag change events.  This fires when a flag is modified.
                        StatusEvent.EVENT_TYPE.EVALUATION_CHANGE -> {
                            Log.i("SDKEvent", "received ${event.eventType} event for flag")
                            event.extractEvaluationPayload()
                            flagValue = client.boolVariation(flagName, false)
                            printMessage("$flagName : $flagValue")
                        }

                        StatusEvent.EVENT_TYPE.EVALUATION_RELOAD -> {
                            Log.i("SDKEvent", "received ${event.eventType} event for flag")
                            event.extractEvaluationListPayload()
                            flagValue = client.boolVariation(flagName, false)
                            printMessage("$flagName : $flagValue")
                        }
                        // There's been an interruption SSE stream which has since resumed, which means the
                        // cache will have been updated with the latest values, so we can call
                        // bool variation to get the most up to date evaluation value.
                        StatusEvent.EVENT_TYPE.SSE_RESUME -> {
                            Log.i("SDKEvent", "received ${event.eventType} event for flag")
                            flagValue = client.boolVariation(flagName, false)
                            printMessage("$flagName : $flagValue")
                        }

                        else -> Log.i("SDKEvent", "Got ${event.eventType.name}")
                    }
                }
            } else {
                Log.e("SDKInit", "Timed out waiting for client to initialize")
            }

        }
    }

    // printMessage uses the UI Thread to update the text on the display
    private fun printMessage(msg: String) {
        val tv1: TextView = findViewById(R.id.textView1)
        runOnUiThread { tv1.text = msg }
    }
}