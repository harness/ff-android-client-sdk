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

    private var client1: CfClient? = null
    private var client2: CfClient? = null
    private var activeClient: CfClient? = null
    private var flagName: String = ""


    private val client1APIKey: String = ""
    private val client2APIKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Use Client 1
        initializeClient(client1, client1APIKey, "https://config.ff.harness.io/api/1.0", "https://events.ff.harness.io/api/1.0")
        useClient(client1)

        // Close Client 1 and use Client 2
        client1?.close()
        initializeClient(client2, client2APIKey, "https://config.ff.harness.io/api/1.0", "https://events.ff.harness.io/api/1.0")
        useClient(client2)

        // Close Client 2 and go back to Client 1
        client2?.close()
        initializeClient(client1, client1APIKey, "https://config.ff.harness.io/api/1.0", "https://events.ff.harness.io/api/1.0")
        useClient(client1)
    }

    private fun initializeClient(client: CfClient?, apiKey: String, baseUrl: String, eventUrl: String) {
        val config = createConfig(baseUrl, eventUrl)
        val target = createTarget()

        if (client == client1) {
            client1 = createClient(apiKey, config, target)
        } else if (client == client2) {
            client2 = createClient(apiKey, config, target)
        }
    }

    private fun createConfig(baseUrl: String, eventUrl: String): CfConfiguration {
        return CfConfiguration.builder()
            .baseUrl(baseUrl)
            .eventUrl(eventUrl)
            .enableStream(true)
            .enablePolling(true)
            .build()
    }

    private fun createTarget(): Target {
        return Target().identifier("ff-android").name("FF Android").apply {
            attributes["location"] = "emea"
        }
    }

    private fun createClient(apiKey: String, config: CfConfiguration, target: Target): CfClient {
        val client = CfClient()
        client.initialize(this, apiKey, config, target) { info, result ->
            if (result.isSuccess) {
                val flagValue: Boolean = client.boolVariation(flagName, false)
                updateTextField("Initialized client with ${config.baseURL}: $flagName : $flagValue")
            } else {
                updateTextField("Failed to initialize client with ${config.baseURL}: ${result.error}")
            }
        }

        setupEventListener(client)
        return client
    }

    private fun setupEventListener(client: CfClient) {
        var flagValue = false
        client.registerEventsListener { event ->
            when (event.eventType) {
                // Setup Listener to handle flag change events. This fires when a flag is modified.
                StatusEvent.EVENT_TYPE.EVALUATION_CHANGE -> {
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

    private fun useClient(client: CfClient?) {
        activeClient = client
        val flagValue: Boolean = activeClient?.boolVariation(flagName, false) ?: false
        updateTextField("Flag value: $flagName : $flagValue")
    }

    private fun updateTextField(msg: String) {
        val tv1: TextView = findViewById(R.id.textView1)
        runOnUiThread { tv1.text = msg }
    }
}
