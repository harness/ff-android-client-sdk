package io.harness.cfsdk.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.core.model.Evaluation
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.oksse.EventsListener
import io.harness.cfsdk.cloud.oksse.model.StatusEvent
import io.harness.cfsdk.logging.CfLog
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val clients = mutableListOf<CfClient>()
    private val logTag = MainActivity::class.simpleName

    companion object {

        private val keys = mutableMapOf<String, String>()
        private val executor = Executors.newSingleThreadExecutor()

        private const val FREEMIUM_API_KEY = "5ca0380d-3209-4322-ae0f-903c58c24457"
        private const val NON_FREEMIUM_API_KEY = "e3019700-b95f-4665-8499-ca184074199c"
    }

    private var eventsListener = EventsListener { event ->

        CfLog.OUT.v(logTag, "Event: ${event.eventType}")

        if (event.eventType == StatusEvent.EVENT_TYPE.EVALUATION_CHANGE) {

            val evaluation: Evaluation = event.extractPayload()
            CfLog.OUT.v(logTag, "Evaluation changed: $evaluation")

        } else if (event.eventType == StatusEvent.EVENT_TYPE.EVALUATION_RELOAD) {

            CfLog.OUT.v(logTag, "Evaluation reload")
        }
    }

    private val flag1Listener: EvaluationListener = EvaluationListener {

        val eval = CfClient.getInstance().boolVariation("flag1", false)
        CfLog.OUT.v(logTag, "flag1 value: $eval")
    }

    private val flag2Listener: EvaluationListener = EvaluationListener {

        val eval = CfClient.getInstance().numberVariation("flag2", -1.0)
        CfLog.OUT.v(logTag, "flag2 value: $eval")
    }

    private val flag3Listener: EvaluationListener = EvaluationListener {

        val eval = CfClient.getInstance().stringVariation("flag3", "NO_VALUE!!!")
        CfLog.OUT.v(logTag, "flag3 value: $eval")
    }

    private val flag4Listener: EvaluationListener = EvaluationListener {

        val eval = CfClient.getInstance().jsonVariation("flag4", JSONObject())
        CfLog.OUT.v(logTag, "flag4 value: $eval")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keys["Freemium"] = FREEMIUM_API_KEY
        keys["Non-Freemium"] = NON_FREEMIUM_API_KEY

        val target = Target().identifier("Harness").name("Harness")

        val remoteConfiguration = CfConfiguration.builder()
            .enableAnalytics(true)
            //            .baseUrl("https://config.feature-flags.uat.harness.io/api/1.0")
            //            .eventUrl("https://event.feature-flags.uat.harness.io/api/1.0")
            //            .streamUrl("https://config.feature-flags.uat.harness.io/api/1.0/stream")
            .enableStream(true)
            .build()

        keys.forEach { keyName, apiKey ->

            executor.execute {

                val client = CfClient()
                clients.add(client)

                client.initialize(

                    this,
                    apiKey,
                    remoteConfiguration,
                    target

                ) { _, result ->

                    if (result.isSuccess) {

                        val registerEventsOk =
                            CfClient.getInstance().registerEventsListener(eventsListener)

                        var registerEvaluationsOk = 0

                        if (client.registerEvaluationListener("flag1", flag1Listener)) {

                            registerEvaluationsOk++
                        }

                        if (client.registerEvaluationListener("flag2", flag2Listener)) {

                            registerEvaluationsOk++
                        }

                        if (client.registerEvaluationListener("flag3", flag3Listener)) {

                            registerEvaluationsOk++
                        }

                        if (client.registerEvaluationListener("flag4", flag4Listener)) {

                            registerEvaluationsOk++
                        }

                        if (registerEventsOk && registerEvaluationsOk == 4) {

                            CfLog.OUT.i(logTag, "Registrations OK")
                        }

                        val bVal = client.boolVariation("flag1", false)
                        CfLog.OUT.v(logTag, "flag1: $bVal")

                        val nVal = client.numberVariation("flag2", -1.0)
                        CfLog.OUT.v(logTag, "flag2: $nVal")

                        val sVal = client.stringVariation("flag3", "NO_VALUE!!!")
                        CfLog.OUT.v(logTag, "flag3: $sVal")

                        val jVal = client.jsonVariation("flag4", JSONObject())
                        CfLog.OUT.v(logTag, "flag4: $jVal")

                    } else {

                        val e = result.error
                        var msg = "Initialization error"
                        e?.let { err ->

                            err.message?.let { errMsg ->
                                msg = errMsg
                            }
                            CfLog.OUT.e(logTag, msg, err)
                        }

                        runOnUiThread {

                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        clients.forEach { client ->

            client.destroy()
        }
    }
}