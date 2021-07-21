package io.harness.cfsdk.testwrapper

data class WrapperTestConfiguration(

    val selfTest: Boolean = true
) {

    companion object {

        const val CONFIGURATION_FILE = "wrapper.json"
    }
}