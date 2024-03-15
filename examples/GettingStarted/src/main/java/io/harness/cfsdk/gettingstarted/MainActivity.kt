package io.harness.cfsdk.gettingstarted

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ch.qos.logback.classic.android.BasicLogcatConfigurator
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.sse.StatusEvent

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            BasicLogcatConfigurator.configureDefaultContext() // Enable SDK logging to logcat
        }
    }

    private lateinit var client: CfClient
    private var flagName: String = BuildConfig.FF_FLAG_NAME.ifEmpty { "harnessappdemodarkmode" }

    // The SDK API Key to use for authentication.  Configure it when installing the app by setting FF_API_KEY
    // e.g. FF_API_KEY='my key' ./gradlew installDebug
    private val apiKey: String = BuildConfig.FF_API_KEY

    enum class InitMethod {
        CALLBACK,
        WAIT_FOR_INIT,
        NON_BLOCKING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Change this to try the different initialization methods
        initializeSdk(InitMethod.CALLBACK)
    }

    private fun initializeSdk(method: InitMethod) {
        updateTextField("initializing SDK using ${method.toString().lowercase()}")
        // Common configuration and target setup
        val streamingEnabled = true
        val pollingEnabled = true
        val sdkConfiguration = CfConfiguration.builder().enableStream(streamingEnabled).enablePolling(pollingEnabled).build()
        val target = Target().identifier("ff-android").name("FF Android")
        target.attributes["location"] = "emea"


        client = CfClient()

        // Setup Listener to handle different events emitted by the SDK
        // You only need to set this up if streaming/polling is enabled.
        if (streamingEnabled && pollingEnabled) {
            setupEventListener()
        }


        when (method) {
            InitMethod.CALLBACK -> initializeWithCallback(sdkConfiguration, target)
            InitMethod.WAIT_FOR_INIT -> initializeWithWaitForInit(sdkConfiguration, target)
            InitMethod.NON_BLOCKING -> initializeNonBlocking(sdkConfiguration, target)
        }

    }

    private fun setupEventListener() {
        var flagValue = false
        client.registerEventsListener { event ->
            when (event.eventType) {
                // Setup Listener to handle flag change events.  This fires when a flag is modified.
                StatusEvent.EVENT_TYPE.EVALUATION_CHANGE-> {
                    flagValue = client.boolVariation(flagName, false)
                    updateTextField("Streamed value for $flagName : $flagValue")
                }

                // This event is fired on the initial poll when the SDK starts up. So it will
                // always be triggered at least once, even if polling is disabled.
                // It is good to subscribe to this event, because the SDK will fallback to polling
                // while streaming recovers from any errors.
                StatusEvent.EVENT_TYPE.EVALUATION_RELOAD -> {
                    flagValue = client.boolVariation(flagName, false)
                    updateTextField("Polled value for $flagName : $flagValue")
                }
                // There's been an interruption SSE stream which has since resumed, which means the
                // cache will have been updated with the latest values, so we can call
                // bool variation to get the most up-to-date evaluation value.
                StatusEvent.EVENT_TYPE.SSE_RESUME -> {
                    flagValue = client.boolVariation(flagName, false)
                    updateTextField("$flagName : $flagValue")
                }

                StatusEvent.EVENT_TYPE.EVALUATION_REMOVE -> {
                    if (event.extractEvaluationPayload().flag == flagName) {
                        Log.w(
                            "SDKEvent",
                            "Flag $flagName was deleted in Harness, ensure this is cleaned up in code"
                        )
                        updateTextField("$flagName was deleted in Harness, ensure this is cleaned up in code")
                    }
                }


                else -> Log.i("SDKEvent", "Got ${event.eventType.name}")
            }
        }
    }

    /* Non-blocking options */
    private fun initializeWithCallback(config: CfConfiguration, target: Target) {
        client.initialize(this, apiKey, config, target) { info, result ->
            if (result.isSuccess) {
                val flagValue: Boolean = client.boolVariation(flagName, false)
                updateTextField("Using callback: $flagName : $flagValue")
            } else {
                updateTextField("Callback: SDK initialization failed: ${result.error}")
            }
        }
    }

    // Highly likely to serve default value of `false` as the SDK will still be initializing.
    // SDKCODE(eval:6001) will be logged if the default variation is served.
    //
    // However, if you subscribe to `EVALUATION_RELOAD` which is fired on successful init and then when the SDK
    // polls, you can evaluate the flag there again to get the current value.
    private fun initializeNonBlocking(config: CfConfiguration, target: Target) {
        client.initialize(this, apiKey, config, target)

        val flagValue: Boolean = client.boolVariation(flagName, false)
        updateTextField("Using non-blocking: $flagName : $flagValue")
    }

    // Blocking option - will block UI thread! Use callback approach above if you don't require
    // blocking option.
    private fun initializeWithWaitForInit(config: CfConfiguration, target: Target) {

        client.initialize(this, apiKey, config, target)
        val success = client.waitForInitialization(60_000)

        if (success) {
            val flagValue: Boolean = client.boolVariation(flagName, false)
            updateTextField("Using callback: $flagName : $flagValue")
        } else {
            updateTextField("WaitForInit: SDK initialization timed out")
        }

    }

    private fun updateTextField(msg: String) {
        val tv1: TextView = findViewById(R.id.textView1)
        runOnUiThread { tv1.text = msg }
    }


}
