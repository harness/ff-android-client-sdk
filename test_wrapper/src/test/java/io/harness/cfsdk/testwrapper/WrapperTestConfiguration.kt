package io.harness.cfsdk.testwrapper

import io.harness.cfsdk.testwrapper.logging.LoggerType

data class WrapperTestConfiguration(

    val selfTest: Boolean = true,
    val port: Int = 4000,
    val apiKey: String,
    val logger: String = LoggerType.DEFAULT.type
) {

    companion object {

        const val CONFIGURATION_FILE = "wrapper.json"
    }
}