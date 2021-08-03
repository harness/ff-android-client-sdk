package io.harness.cfsdk.testwrapper.context

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.harness.cfsdk.BuildConfig
import io.harness.cfsdk.testwrapper.context.api.VersionResponse
import io.harness.cfsdk.testwrapper.request.REQUEST_METHOD
import java.io.ByteArrayInputStream
import java.io.IOException

internal class SimpleContextFactory : CommonContextFactory() {

    companion object {

        const val PATH_VERSION = "/sdk/version"
    }

    override fun build(server: HttpServer) {

        server.createContext(PATH_VERSION) { exchange -> handleExchange(exchange) }
    }

    private fun handleExchange(exchange: HttpExchange) {

        try {

            when (exchange.requestMethod) {

                REQUEST_METHOD.GET -> {

                    when (exchange.requestURI.path) {

                        PATH_VERSION -> {

                            val version = VersionResponse(BuildConfig.APP_VERSION_NAME)
                            val json = Gson().toJson(version)
                            exchange.sendResponseHeaders(200, 0)
                            val output = exchange.responseBody
                            val input = ByteArrayInputStream(json.toByteArray())
                            input.copyTo(output)
                            input.close()
                            output.close()
                        }
                        else -> {

                            err404(exchange)
                        }
                    }
                }
            }
        } catch (e: IOException) {

            err500(exchange, e)
        }
    }
}