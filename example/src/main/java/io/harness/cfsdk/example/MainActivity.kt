package io.harness.cfsdk.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Toast

import io.harness.cfsdk.*
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.logging.CfLog

class MainActivity : AppCompatActivity() {

    private val logTag = MainActivity::class.simpleName

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