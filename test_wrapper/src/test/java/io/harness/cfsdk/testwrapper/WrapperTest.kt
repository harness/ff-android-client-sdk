package io.harness.cfsdk.testwrapper

import io.harness.cfsdk.logging.CfLog
import io.harness.cfsdk.testwrapper.environment.Variables
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WrapperTest {

    /**
     * Start the wrapper server and execute tests
     *
     * True == Execute tests and shutdown the server
     * False == Start server and wait for 3rd party to perform the tests.
     */
    private var selfTest = true

    private val server = WrapperServer()
    private val tag = WrapperTest::class.simpleName

    @Before
    fun setup() {

        CfLog.testModeOn()

        try {

            val envSelfTest = System.getenv(Variables.ENV_VAR_SELF_TEST)
            CfLog.OUT.v(tag, "Environment variable ${Variables.ENV_VAR_SELF_TEST}: $envSelfTest")

            // TODO:

        } catch (e: NullPointerException) {

            Assert.fail(e.message)
        } catch (e: SecurityException) {

            Assert.fail(e.message)
        }
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

        // TODO:

        return true
    }

    private fun terminateLocalServer(): Boolean {

        CfLog.OUT.v(tag, "Shutting down local server")
        return server.shutdown()
    }
}