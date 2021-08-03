package io.harness.cfsdk.testwrapper.context

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.logging.CfLog
import io.harness.cfsdk.testwrapper.context.api.FlagCheckRequest
import io.harness.cfsdk.testwrapper.context.api.FlagCheckResponse
import io.harness.cfsdk.testwrapper.context.api.KIND
import io.harness.cfsdk.testwrapper.context.api.PongResponse
import io.harness.cfsdk.testwrapper.request.REQUEST_METHOD
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.IllegalArgumentException

class ApiContextFactory : CommonContextFactory() {

    private val tag = ApiContextFactory::class.simpleName

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

                            val reader = BufferedReader(exchange.requestBody.reader())

                            val content: String
                            reader.use {

                                content = it.readText()
                                it.close()
                            }

                            val request = Gson().fromJson(content, FlagCheckRequest::class.java)

                            val key = request.flagKey
                            val kind = request.flagKind

                            CfLog.OUT.v(tag, "key=$key, kind=$kind")

                            val flagValue : Any = when (kind) {

                                KIND.BOOLEAN.value -> {

                                    CfClient.getInstance().boolVariation(key, false)
                                }
                                KIND.INT.value -> {

                                    CfClient.getInstance().numberVariation(key, 0.0)
                                }
                                KIND.STRING.value -> {

                                    CfClient.getInstance().stringVariation(key, "")
                                }
                                KIND.JSON.value -> {

                                    val o = CfClient.getInstance().jsonVariation(key, JSONObject())
                                    Gson().toJson(o)
                                }
                                else -> throw IllegalArgumentException("Unknown kind: '$kind'")
                            }

                            CfLog.OUT.v(tag, "Flag value: $flagValue")

                            val checkFlagResponse = FlagCheckResponse(

                                key,
                                flagValue.toString()
                            )

                            val json = Gson().toJson(checkFlagResponse)
                            CfLog.OUT.v(tag, "JSON: $json")

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
                else -> err404(exchange)
            }
        } catch (e: IllegalArgumentException) {

            err500(exchange, e)
        } catch (e: IOException) {

            err500(exchange, e)
        } catch (e: JsonSyntaxException) {

            err500(exchange, e)
        }
    }
}