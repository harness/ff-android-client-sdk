package io.harness.cfsdk.testwrapper

class WrapperServer : Initialization, Termination {

    override fun init(): Boolean {

        return false
    }

    override fun shutdown(): Boolean {

        return false
    }
}