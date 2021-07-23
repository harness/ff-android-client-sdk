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

class MainActivity : AppCompatActivity() {

    private val logTag = MainActivity::class.simpleName

    private var eventsListener = EventsListener { event ->

        CfLog.OUT.v(logTag, "Event: ${event.eventType}")

        if (event.eventType == StatusEvent.EVENT_TYPE.EVALUATION_CHANGE) {

            val evaluation: Evaluation = event.extractPayload()
            CfLog.OUT.v(logTag, "Evaluation changed: $evaluation")

        } else if (event.eventType == StatusEvent.EVENT_TYPE.EVALUATION_RELOAD) {

            CfLog.OUT.v(logTag, "Evaluation reload")
        }
    }

    private val darkModeListener: EvaluationListener = EvaluationListener {

        val eval = CfClient.getInstance().boolVariation(Const.FF_DARK_MODE, false)
        CfLog.OUT.v(logTag, "Dark mode value: $eval")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val target = Target().identifier("Harness").name("Harness")

        val remoteConfiguration = CfConfiguration.builder()
            .enableAnalytics(true)
//            .baseUrl("https://config.feature-flags.uat.harness.io/api/1.0")
//            .eventUrl("https://event.feature-flags.uat.harness.io/api/1.0")
//            .streamUrl("https://config.feature-flags.uat.harness.io/api/1.0/stream")
            .enableStream(true)
            .build()

        CfClient.getInstance()
            .initialize(

                this,
                Const.API_KEY,
                remoteConfiguration,
                target
            ) { _, result ->

                runOnUiThread {

                    if (result.isSuccess) {

                        val registerEventsOk = CfClient.getInstance().registerEventsListener(eventsListener)

                        val registerEvaluationsOk = CfClient.getInstance().registerEvaluationListener(

                            Const.FF_DARK_MODE,
                            darkModeListener
                        )

                        if (registerEventsOk && registerEvaluationsOk) {

                            CfLog.OUT.v(logTag, "Registrations OK")
                        }

                        var eval = CfClient.getInstance().boolVariation("firstbooleanflag", false)
                        CfLog.OUT.v(logTag, "firstbooleanflag: $eval")

                        eval = CfClient.getInstance().boolVariation("somethingelse", false)
                        CfLog.OUT.v(logTag, "somethingelse: $eval")

                        eval = CfClient.getInstance().boolVariation(Const.FF_DARK_MODE, false)
                        CfLog.OUT.v(logTag, "${Const.FF_DARK_MODE}: $eval")

                    } else {

                        val e = result.error
                        var msg = "Initialization error"
                        e?.let { err ->

                            err.message?.let { errMsg ->
                                msg = errMsg
                            }
                            CfLog.OUT.e(logTag, msg, err)
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        CfClient.getInstance().destroy()
    }
}