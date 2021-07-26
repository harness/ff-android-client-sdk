package io.harness.cfsdk.testwrapper

import com.sun.net.httpserver.HttpServer
import io.harness.cfsdk.testwrapper.capability.Initialization
import io.harness.cfsdk.testwrapper.capability.Status
import io.harness.cfsdk.testwrapper.capability.Termination
import io.harness.cfsdk.testwrapper.context.ApiContextFactory
import io.harness.cfsdk.testwrapper.context.SimpleContextFactory
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class WrapperServer(port: Int) : Initialization, Termination, Status {

    private val running = AtomicBoolean()
    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0)

    private val serverContextFactories = listOf(

        SimpleContextFactory(),
        ApiContextFactory()
    )

    override fun init(): Boolean {

        server.executor = Executors.newFixedThreadPool(10)
        server.start()

        serverContextFactories.forEach {

            it.build(server)
        }

        // TODO: Init SDK

        running.set(true)
        return running.get()
    }

    override fun shutdown(): Boolean {

        running.set(false)
        server.stop(0)
        return isNotActive()
    }

    override fun isActive() = running.get()
}