package io.harness.cfsdk.testwrapper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.logging.CfLog
import io.harness.cfsdk.testwrapper.context.api.*
import io.harness.cfsdk.utils.CfUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class WrapperTest {

    /**
     * Start the wrapper server and execute the tests.
     *
     * True == Start the local server, execute tests and shutdown the server.
     * False == Start the local server and wait for 3rd party to perform the tests.
     */
    private var selfTest = true

    /**
     * Port to be used by local server instance.
     */
    private var serverPort = 4000

    /**
     * API key used to initialize the SDK.
     */
    private var apiKey = "YOUR_API_KEY"

    lateinit var server: WrapperServer
    private val tag = WrapperTest::class.simpleName

    @Before
    fun setup() {

        CfLog.testModeOn()

        var inputStream: InputStream? = null
        try {

            inputStream = File(WrapperTestConfiguration.CONFIGURATION_FILE).inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            val config = Gson().fromJson(inputString, WrapperTestConfiguration::class.java)

            CfLog.OUT.v(tag, "$config")

            selfTest = config.selfTest
            serverPort = config.port
            apiKey = config.apiKey

        } catch (e: NullPointerException) {

            Assert.fail(e.message)
        } catch (e: SecurityException) {

            Assert.fail(e.message)
        } catch (e: JsonSyntaxException) {

            Assert.fail(e.message)
        } catch (e: FileNotFoundException) {

            CfLog.OUT.v(tag, "No test configuration file provided")
        } finally {

            inputStream?.let {

                try {

                    it.close()
                } catch (e: IOException) {

                    CfLog.OUT.w(tag, e)
                }
            }
        }

        val configuration = CfConfiguration.Builder()
            .enableAnalytics(true)
            .enableStream(true)
            .pollingInterval(60)
            .build()

        val wTest = "wrapper_test"
        val target = Target().identifier(wTest).name(wTest)

        server = WrapperServer(

            port = serverPort,
            apiKey = apiKey,
            target = target,
            configuration = configuration
        )
    }

    @Test
    fun testSDK() {

        Assert.assertTrue(

            initLocalServer()
        )

        CfLog.OUT.v(tag, "Local server is running")

        if (selfTest) {

            Assert.assertTrue(

                runTests()
            )

            CfLog.OUT.v(tag, "Test have been executed")

            Assert.assertTrue(

                terminateLocalServer()
            )

            CfLog.OUT.v(tag, "Local server has been shut down")
        } else {

            while (server.isActive()) {

                Thread.yield()
            }
        }
    }

    private fun initLocalServer(): Boolean {

        CfLog.OUT.v(tag, "Initializing local server")
        return server.init()
    }

    private fun runTests(): Boolean {

        CfLog.OUT.v(tag, "Running tests")

        val calls = mutableListOf<Call<*>>()
        val flagChecks = mutableMapOf<KIND, Call<FlagCheckResponse>>()

        val gsonBuilder = GsonBuilder()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:$serverPort/")
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .build()

        val apiContextService = retrofit.create(ApiContextService::class.java)
        val simpleContextService = retrofit.create(SimpleContextService::class.java)

        val flagCheckRequests = mapOf(

            KIND.BOOLEAN to "flag1",
            KIND.INT to "flag2",
            KIND.STRING to "flag3",
            KIND.JSON to "flag4"
        )

        calls.addAll(

            listOf(

                apiContextService.ping(),
                simpleContextService.version()
            )
        )

        flagCheckRequests.forEach { (key, value) ->

            flagChecks[key] = apiContextService.checkFlag(FlagCheckRequest(key.value, value))
        }

        calls.forEach { request ->

            val response = request.execute()
            val msg = getMsg(request, response)

            if (!response.isSuccessful) {

                CfLog.OUT.e(tag, getErrMsg(msg, response))
                return false
            }

            CfLog.OUT.i(tag, msg)
        }

        flagChecks.forEach { (key, request) ->

            val response = request.execute()
            val msg = getMsg(request, response)

            if (!response.isSuccessful) {

                CfLog.OUT.e(tag, getErrMsg(msg, response))
                return false
            }

            CfLog.OUT.i(tag, msg)

            when (key) {

                KIND.BOOLEAN -> {

                    val value = response.body()
                    Assert.assertNotNull(value)

                    value?.let { v ->

                        val b = v.flagValue.toBoolean()
                        Assert.assertTrue(b)
                    }
                }
                KIND.INT -> {

                    val value = response.body()
                    Assert.assertNotNull(value)

                    value?.let { v ->

                        val no = v.flagValue.toDouble()
                        Assert.assertTrue(no > 0)
                    }
                }
                KIND.STRING -> {

                    val value = response.body()
                    Assert.assertNotNull(value)

                    value?.let { v ->

                        val s = v.flagValue
                        Assert.assertTrue(CfUtils.Text.isNotEmpty(s))
                    }
                }
                KIND.JSON -> {

                    val value = response.body()
                    Assert.assertNotNull(value)

                    value?.let { v ->

                        val j = v.flagValue
                        Assert.assertTrue(CfUtils.Text.isNotEmpty(j))
                    }
                }
                else -> Assert.fail("Unknown kind: '$key'")
            }
        }

        return true
    }

    private fun terminateLocalServer(): Boolean {

        CfLog.OUT.v(tag, "Shutting down local server")
        return server.shutdown()
    }

    private fun getErrMsg(

        msg: String,
        response: Response<out Any>

    ) = "$msg, error=\"${response.errorBody()?.string()}\""

    private fun getMsg(

        it: Call<*>,
        response: Response<out Any>

    ) = "url=${it.request().url} code=${response.code()}, payload=${response.body()}"
}