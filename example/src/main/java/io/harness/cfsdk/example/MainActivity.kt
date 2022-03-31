package io.harness.cfsdk.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.oksse.EventsListener
import io.harness.cfsdk.logging.CfLog
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val clients = mutableListOf<CfClient>()
    private val timers = mutableMapOf<String, Timer>()
    private val logTag = MainActivity::class.simpleName

    companion object {

        private val keys = mutableMapOf<String, String>()
        private val executor = Executors.newSingleThreadExecutor()

        private const val KEY_UAT = "UAT"

        private const val UAT_KEY = "960a7bda-c1c6-4593-b174-e4a3b7f4ce76"
        private const val FREEMIUM_API_KEY = "a6efdb72-4c01-45e0-8285-1dcbd63f72e7"
        private const val NON_FREEMIUM_API_KEY = "d122149a-fadd-471d-ab31-7938a2b90ba2"
    }

    private var eventsListener = EventsListener { event ->

        CfLog.OUT.v(logTag, "Event: ${event.eventType}")
    }

    private val flag1Listener: EvaluationListener = EvaluationListener {

        clients.forEach { client ->

            val eval = client.boolVariation("flag1", false)
            CfLog.OUT.v(logTag, "flag1 value: $eval")
        }
    }

    private val otherFlagListener: EvaluationListener = EvaluationListener {

        clients.forEach { client ->

            val eval = client.boolVariation("harnessappdemoenablecimodule", false)
            CfLog.OUT.v(logTag, "harnessappdemoenablecimodule value: $eval")
        }
    }

    private val flag2Listener: EvaluationListener = EvaluationListener {

        clients.forEach { client ->

            val eval = client.numberVariation("flag2", -1.0)
            CfLog.OUT.v(logTag, "flag2 value: $eval")
        }
    }

    private val flag3Listener: EvaluationListener = EvaluationListener {

        clients.forEach { client ->

            val eval = client.stringVariation("flag3", "NO_VALUE!!!")
            CfLog.OUT.v(logTag, "flag3 value: $eval")
        }
    }

    private val flag4Listener: EvaluationListener = EvaluationListener {

        clients.forEach { client ->

            val eval = client.jsonVariation("flag4", JSONObject())
            CfLog.OUT.v(logTag, "flag4 value: $eval")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keys[KEY_UAT] = UAT_KEY
//        keys["Freemium"] = FREEMIUM_API_KEY
//        keys["Non-Freemium"] = NON_FREEMIUM_API_KEY

        val uuid = UUID.randomUUID().toString()
        val target = Target().identifier(uuid).name(uuid)

        keys.forEach { (keyName, apiKey) ->

            executor.execute {

                val client = CfClient()
                clients.add(client)

                val logPrefix = keyName + " :: " + client.hashCode()

                val builder = CfConfiguration.builder()
                    .enableAnalytics(true)

                if (keyName == KEY_UAT) {

                    CfLog.OUT.v(logTag, "Setting up the UAT url(s)")

                    builder
                        .baseUrl("https://config.feature-flags.uat.harness.io/api/1.0")
                        .eventUrl("https://event.feature-flags.uat.harness.io/api/1.0")
                        .streamUrl("https://config.feature-flags.uat.harness.io/api/1.0/stream")
                }

                val config = builder
                    .enableStream(true)
                    .build()

                client.initialize(

                    this,
                    apiKey,
                    config,
                    target

                ) { _, result ->

                    readEvaluations(client, "$logPrefix PRE :: ")

                    if (result.isSuccess) {

                        val registerEventsOk = client.registerEventsListener(eventsListener)

                        var registerEvaluationsOk = 0

                        if (client.registerEvaluationListener("flag1", flag1Listener)) {

                            registerEvaluationsOk++
                        }

                        if (
                            client.registerEvaluationListener(

                                "harnessappdemoenablecimodule",
                                otherFlagListener
                            )
                        ) {

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

                            CfLog.OUT.i(logTag, "$logPrefix Registrations OK")
                        }

                        try {

                            val timer = Timer()
                            timers[keyName] = timer

                            timer.schedule(

                                object : TimerTask() {

                                    override fun run() {

                                        readEvaluations(client, logPrefix)
                                    }
                                },
                                0,
                                10000
                            )

                        } catch (e: IllegalStateException) {

                            CfLog.OUT.e(logTag, "Error", e)

                        } catch (e: IllegalArgumentException) {

                            CfLog.OUT.e(logTag, "Error", e)

                        } catch (e: NullPointerException) {

                            CfLog.OUT.e(logTag, "Error", e)
                        }

                    } else {

                        val e = result.error
                        var msg = "$logPrefix Initialization error"
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

    private fun readEvaluations(client: CfClient, logPrefix: String) {

        var bVal = client.boolVariation("flag1", false)
        CfLog.OUT.v(logTag, "$logPrefix flag1: $bVal")
//
//        val nVal = client.numberVariation("flag2", -1.0)
//        CfLog.OUT.v(logTag, "$logPrefix flag2: $nVal")
//
//        val sVal = client.stringVariation("flag3", "NO_VALUE!!!")
//        CfLog.OUT.v(logTag, "$logPrefix flag3: $sVal")
//
//        val jVal = client.jsonVariation("flag4", JSONObject())
//        CfLog.OUT.v(logTag, "$logPrefix flag4: $jVal")
//
//        bVal = client.boolVariation("harnessappdemoenablecimodule", false)
//        CfLog.OUT.v(logTag, "$logPrefix harnessappdemoenablecimodule: $bVal")
    }

    override fun onDestroy() {
        super.onDestroy()

        timers.values.forEach {

            it.cancel()
            it.purge()
        }

        timers.clear()

        clients.forEach { client ->

            client.destroy()
        }
    }
}