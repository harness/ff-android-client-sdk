package io.harness.cfsdk.testwrapper

import io.harness.cfsdk.testwrapper.capability.Initialization
import io.harness.cfsdk.testwrapper.capability.Status
import io.harness.cfsdk.testwrapper.capability.Termination
import java.util.concurrent.atomic.AtomicBoolean

class WrapperServer : Initialization, Termination, Status {

    private val running = AtomicBoolean()

    override fun init(): Boolean {

        running.set(true)

        // TODO:

        return running.get()
    }

    override fun shutdown(): Boolean {

        running.set(false)

        // TODO:

        return isNotActive()
    }

    override fun isActive() = running.get()
}