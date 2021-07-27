package io.harness.cfsdk.testwrapper

import com.sun.net.httpserver.HttpServer
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.events.AuthCallback
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.logging.CfLog
import io.harness.cfsdk.testwrapper.capability.Initialization
import io.harness.cfsdk.testwrapper.capability.Status
import io.harness.cfsdk.testwrapper.capability.Termination
import io.harness.cfsdk.testwrapper.client.WrapperClient
import io.harness.cfsdk.testwrapper.context.ApiContextFactory
import io.harness.cfsdk.testwrapper.context.SimpleContextFactory
import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class WrapperServer(

    private val port: Int,
    private val apiKey: String,
    private val target: Target,
    private val configuration: CfConfiguration

) : Initialization, Termination, Status {

    private val running = AtomicBoolean()
    private val tag = WrapperServer::class.simpleName
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

        var success = false
        val latch = CountDownLatch(1)

        (WrapperClient.getInstance() as WrapperClient).initialize(

            apiKey,
            configuration,
            target

        ) { authInfo, result ->

            if (authInfo == null) {

                CfLog.OUT.e(tag, "No auth info!")
            } else {

                CfLog.OUT.v(tag, "Auth info: $authInfo")
            }

            success = result.isSuccess
            if (!success) {

                CfLog.OUT.e(tag, "SDK auth. failed")
            }
            latch.countDown()
        }

        latch.await()

        running.set(success)
        return running.get()
    }

    override fun shutdown(): Boolean {

        running.set(false)
        server.stop(0)
        return isNotActive()
    }

    override fun isActive() = running.get()
}