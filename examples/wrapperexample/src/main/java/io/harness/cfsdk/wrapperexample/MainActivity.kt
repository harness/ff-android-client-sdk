package io.harness.cfsdk.wrapperexample


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.sse.EventsListener
import io.harness.cfsdk.cloud.sse.StatusEvent
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var flagWrapper: HarnessFfSdkWrapper
    private val apiKey: String = "YOUR_API_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flagWrapper = HarnessFfSdkWrapper()

        val config = CfConfiguration.builder()
            .enableStream(true)
            .enablePolling(true)
            .build()

        val target = Target().identifier("ff-android").name("FF Android")
        target.attributes["location"] = "emea"

        flagWrapper.initialize(this, apiKey, config, target) { success, error ->
            if (success) {
                Log.i("MainActivity", "Harness FF SDK initialized successfully.")
                setupFlags()
            } else {
                Log.e("MainActivity", "Failed to initialize Harness FF SDK: $error")
            }
        }
    }

    private fun setupFlags() {
        try {
            flagWrapper.addFlag("boolean_flag", HarnessFfSdkWrapper.FlagType.BOOLEAN, HarnessFfSdkWrapper.DefaultValue.BooleanDefault(false))
            flagWrapper.addFlag("string_flag", HarnessFfSdkWrapper.FlagType.STRING, HarnessFfSdkWrapper.DefaultValue.StringDefault("default"))
            flagWrapper.addFlag("number_flag", HarnessFfSdkWrapper.FlagType.NUMBER, HarnessFfSdkWrapper.DefaultValue.NumberDefault(0.0))
            flagWrapper.addFlag("json_flag", HarnessFfSdkWrapper.FlagType.JSON, HarnessFfSdkWrapper.DefaultValue.JsonDefault(JSONObject()))

            val booleanFlagValue = flagWrapper.evaluateBooleanFlag("boolean_flag")
            Log.d("FlagValues", "Boolean Flag: $booleanFlagValue")

            val stringFlagValue = flagWrapper.evaluateStringFlag("string_flag")
            Log.d("FlagValues", "String Flag: $stringFlagValue")

            val numberFlagValue = flagWrapper.evaluateNumberFlag("number_flag")
            Log.d("FlagValues", "Number Flag: $numberFlagValue")

            val jsonFlagValue = flagWrapper.evaluateJsonFlag("json_flag")
            Log.d("FlagValues", "JSON Flag: $jsonFlagValue")

            val areAllFlagsEnabled = flagWrapper.areAllFlagsEnabled()
            Log.d("FlagValues", "Are all flags enabled: $areAllFlagsEnabled")

            val eventListener = EventsListener { event ->
                when (event.eventType) {
                    StatusEvent.EVENT_TYPE.EVALUATION_CHANGE -> {
                        val flagValue = flagWrapper.evaluateBooleanFlag("boolean_flag")
                        Log.d("Event", "Evaluation changed: boolean_flag: $flagValue")
                    }
                    StatusEvent.EVENT_TYPE.EVALUATION_RELOAD -> {
                        val flagValue = flagWrapper.evaluateBooleanFlag("boolean_flag")
                        Log.d("Event", "Evaluation reloaded: boolean_flag: $flagValue")
                    }
                    StatusEvent.EVENT_TYPE.SSE_RESUME -> {
                        val flagValue = flagWrapper.evaluateBooleanFlag("boolean_flag")
                        Log.d("Event", "SSE resumed: boolean_flag: $flagValue")
                    }
                    else -> Log.d("Event", "Event type: ${event.eventType.name}")
                }
            }

            flagWrapper.addEventListener(eventListener)
        } catch (e: Exception) {
            Log.e("Error", e.message ?: "Unknown error")
        }
    }
}
