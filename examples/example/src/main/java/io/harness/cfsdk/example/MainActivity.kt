package io.harness.cfsdk.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.oksse.EventsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val log: Logger = LoggerFactory.getLogger(MainActivity::class.java)

    private val clients = mutableListOf<CfClient>()
    private val timers = mutableMapOf<String, Timer>()

    companion object {

        private val keys = mutableMapOf<String, String>()
        private val executor = Executors.newSingleThreadExecutor()

        private const val KEY_ISSUE = "ISSUE"
        private const val ISSUE_KEY = "9dcd36d5-fdae-4b01-ab32-a7ad2ffd7ee8"
//        private const val KEY_UAT = "UAT"

//        private const val UAT_KEY = "960a7bda-c1c6-4593-b174-e4a3b7f4ce76"
//        private const val FREEMIUM_API_KEY = "a6efdb72-4c01-45e0-8285-1dcbd63f72e7"
//        private const val NON_FREEMIUM_API_KEY = "d122149a-fadd-471d-ab31-7938a2b90ba2"
    }

    private var eventsListener = EventsListener { event ->

        log.debug("Event: ${event.eventType}")
    }

    private val flag1Listener: EvaluationListener = EvaluationListener {

        clients.forEach { client ->

            val eval = client.boolVariation("harnessappdemodarkmode", false)
            log.info("harnessappdemodarkmode value: $eval")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keys[KEY_ISSUE] = ISSUE_KEY

        val targetName = "android1"
        val target = Target().identifier(targetName).name(targetName)

        keys.forEach { (keyName, apiKey) ->

            executor.execute {

                val client = CfClient()
                clients.add(client)

                val logPrefix = keyName + " :: " + client.hashCode()

                val config = CfConfiguration.builder()
                    .enableAnalytics(true)
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

                        if (registerEventsOk && registerEvaluationsOk > 0) {

                            log.info("$logPrefix Registrations OK")
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

                        } catch (e:Exception) {

                            log.error("Error", e)
                        }

                    } else {

                        val e = result.error
                        var msg = "$logPrefix Initialization error"
                        e?.let { err ->

                            err.message?.let { errMsg ->
                                msg = errMsg
                            }
                           log.error(msg, err)
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

        val bVal = client.boolVariation("harnessappdemodarkmode", false)
        log.debug("$logPrefix harnessappdemodarkmode: $bVal")
    }

    override fun onDestroy() {
        super.onDestroy()

        timers.values.forEach {

            it.cancel()
            it.purge()
        }

        timers.clear()

        clients.forEach { client ->

            client.close()
        }
    }
}