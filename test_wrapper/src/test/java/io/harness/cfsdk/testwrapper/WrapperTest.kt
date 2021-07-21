package io.harness.cfsdk.testwrapper

import io.harness.cfsdk.logging.CfLog
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WrapperTest {

    private val selfTest = true

    private val server = WrapperServer()
    private val tag = WrapperTest::class.simpleName

    @Before
    fun setup() {

        CfLog.testModeOn()
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
        }

        Assert.assertTrue(

            terminateLocalServer()
        )

        CfLog.OUT.v(tag, "Local server has been shut down")
    }

    private fun initLocalServer(): Boolean {

        CfLog.OUT.v(tag, "Initializing local server")
        return server.init()
    }

    private fun runTests(): Boolean {

        CfLog.OUT.v(tag, "Running tests")
        return false
    }

    private fun terminateLocalServer(): Boolean {

        CfLog.OUT.v(tag, "Shutting down local server")
        return server.shutdown()
    }
}