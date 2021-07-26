package io.harness.cfsdk.testwrapper.context

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.harness.cfsdk.testwrapper.context.api.PongResponse
import io.harness.cfsdk.testwrapper.request.REQUEST_METHOD
import java.io.ByteArrayInputStream
import java.io.IOException

class ApiContextFactory : CommonContextFactory() {

    companion object {

        const val PATH_PING = "/api/1.0/ping"
        const val PATH_CHECK_FLAG = "/api/1.0/check_flag"
    }

    override fun build(server: HttpServer) {

        server.createContext(PATH_PING) { exchange -> handleExchange(exchange) }
        server.createContext(PATH_CHECK_FLAG) { exchange -> handleExchange(exchange) }
    }

    private fun handleExchange(exchange: HttpExchange) {

        try {

            when (exchange.requestMethod) {

                REQUEST_METHOD.GET -> {

                    when (exchange.requestURI.path) {

                        PATH_PING -> {

                            val pong = PongResponse(true)
                            val json = Gson().toJson(pong)
                            exchange.sendResponseHeaders(200, 0)
                            val output = exchange.responseBody
                            val input = ByteArrayInputStream(json.toByteArray())
                            input.copyTo(output)
                            input.close()
                            output.close()
                        }
                        else -> err404(exchange)
                    }
                }
                REQUEST_METHOD.POST -> {

                    when (exchange.requestURI.path) {

                        PATH_CHECK_FLAG -> {

                            // TODO:
                            err404(exchange)
                        }
                        else -> err404(exchange)
                    }
                }
                else -> err404(exchange)
            }
        } catch (e: IOException) {

            err500(exchange, e)
        }
    }
}