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

    private lateinit var harnessFF: HarnessFfSdkWrapper
    private val apiKey: String = "YOUR_API_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = CfConfiguration.builder()
            .enableStream(true)
            .enablePolling(true)
            .build()

        val target = Target().identifier("ff-android").name("FF Android")
        target.attributes["location"] = "emea"

        harnessFF = HarnessFfSdkWrapper(this, apiKey, config, target)

        harnessFF.addFlag("boolean_flag", HarnessFfSdkWrapper.FlagType.BOOLEAN, HarnessFfSdkWrapper.DefaultValue.BooleanDefault(false))
        harnessFF.addFlag("string_flag", HarnessFfSdkWrapper.FlagType.STRING, HarnessFfSdkWrapper.DefaultValue.StringDefault("default"))
        harnessFF.addFlag("number_flag", HarnessFfSdkWrapper.FlagType.NUMBER, HarnessFfSdkWrapper.DefaultValue.NumberDefault(0.0))
        harnessFF.addFlag("json_flag", HarnessFfSdkWrapper.FlagType.JSON, HarnessFfSdkWrapper.DefaultValue.JsonDefault(JSONObject()))

        val booleanFlagValue = harnessFF.evaluateBooleanFlag("boolean_flag")
        Log.d("FlagValues", "Boolean Flag: $booleanFlagValue")

        val stringFlagValue = harnessFF.evaluateStringFlag("string_flag")
        Log.d("FlagValues", "String Flag: $stringFlagValue")

        val numberFlagValue = harnessFF.evaluateNumberFlag("number_flag")
        Log.d("FlagValues", "Number Flag: $numberFlagValue")

        val jsonFlagValue = harnessFF.evaluateJsonFlag("json_flag")
        Log.d("FlagValues", "JSON Flag: $jsonFlagValue")

        val areAllFlagsEnabled = harnessFF.areAllFlagsEnabled()
        Log.d("FlagValues", "Are all flags enabled: $areAllFlagsEnabled")

        val eventListener = EventsListener { event ->
            when (event.eventType) {
                StatusEvent.EVENT_TYPE.EVALUATION_CHANGE -> {
                    val flagValue = harnessFF.evaluateBooleanFlag("boolean_flag")
                    Log.d("Event", "Evaluation changed: boolean_flag: $flagValue")
                }
                StatusEvent.EVENT_TYPE.EVALUATION_RELOAD -> {
                    val flagValue = harnessFF.evaluateBooleanFlag("boolean_flag")
                    Log.d("Event", "Evaluation reloaded: boolean_flag: $flagValue")
                }
                StatusEvent.EVENT_TYPE.SSE_RESUME -> {
                    val flagValue = harnessFF.evaluateBooleanFlag("boolean_flag")
                    Log.d("Event", "SSE resumed: boolean_flag: $flagValue")
                }
                else -> Log.d("Event", "Event type: ${event.eventType.name}")
            }
        }

        harnessFF.addEventListener(eventListener)
    }
}
