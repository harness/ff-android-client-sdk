package io.harness.cfsdk.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import io.harness.cfsdk.*
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.logging.CfLog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val target = Target().identifier("Harness").name("Harness")
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}