package io.harness.cfsdk.testwrapper.context

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.harness.cfsdk.testwrapper.request.REQUEST_METHOD

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

        when (exchange.requestMethod) {

            REQUEST_METHOD.GET -> {

                when (exchange.requestURI.path) {

                    PATH_PING -> {

                        // TODO:
                        err404(exchange)
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
    }
}