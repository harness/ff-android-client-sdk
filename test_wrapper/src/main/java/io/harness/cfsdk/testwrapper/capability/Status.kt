package io.harness.cfsdk.testwrapper.capability

interface Status {

    fun isActive(): Boolean

    fun isNotActive() = !isActive()
}