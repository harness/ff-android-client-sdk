package io.harness.cfsdk.testwrapper.context

import com.sun.net.httpserver.HttpExchange
import java.io.ByteArrayInputStream

abstract class CommonContextFactory : ContextFactory {

    protected fun err404(exchange: HttpExchange) {

        exchange.sendResponseHeaders(404, 0)
        val output = exchange.responseBody
        val input = ByteArrayInputStream("Not found".toByteArray())
        input.copyTo(output)
        input.close()
        output.close()
    }
}