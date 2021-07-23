package io.harness.cfsdk.testwrapper

data class WrapperTestConfiguration(

    val selfTest: Boolean = true,
    val port: Int = 4000
) {

    companion object {

        const val CONFIGURATION_FILE = "wrapper.json"
    }
}