package io.harness.cfsdk.gettingstarted

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import io.harness.cfsdk.*
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.Target

class MainActivity : AppCompatActivity() {

    // The default flag name in this demo is "harnessappdemodarkmode".
    private val flagName: String = BuildConfig.FF_FLAG_NAME.ifEmpty { "harnessappdemodarkmode" }

    // The SDK API Key to use for authentication.  Configure it when installing the app by setting FF_API_KEY
    // e.g. FF_API_KEY='my key' ./gradlew installDebug
    private val apiKey: String = BuildConfig.FF_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        printMessage("Starting SDK")

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
                CfClient.getInstance().registerEvaluationListener(flagName, flagListener)
            } else {
                Log.e("SDKInit", "Failed to initialize client", result.error)
                result.error.message?.let { printMessage(it) }
            }
        }
    }

    // flagListener can be used to handle an event
    private val flagListener: EvaluationListener = EvaluationListener {
        Log.i("SDKEvent", "received event for flag")
        var flagValue : Boolean = CfClient.getInstance().boolVariation(flagName, false)
        printMessage("$flagName : $flagValue")
    }

    // printMessage uses the UI Thread to update the text on the display
    private fun printMessage(msg : String) {
        val tv1: TextView = findViewById(R.id.textView1)
        runOnUiThread { tv1.text = msg }
    }
}