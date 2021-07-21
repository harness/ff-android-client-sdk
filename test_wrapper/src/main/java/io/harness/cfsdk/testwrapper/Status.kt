package io.harness.cfsdk.testwrapper

interface Status {

    fun isActive(): Boolean

    fun isNotActive() = !isActive()
}