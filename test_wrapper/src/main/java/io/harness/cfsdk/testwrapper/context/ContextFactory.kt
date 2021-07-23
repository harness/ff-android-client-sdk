package io.harness.cfsdk.testwrapper.context

import com.sun.net.httpserver.HttpServer

internal interface ContextFactory {

    fun build(server: HttpServer)
}