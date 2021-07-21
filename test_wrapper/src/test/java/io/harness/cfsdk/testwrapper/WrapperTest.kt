package io.harness.cfsdk.testwrapper

import org.junit.Assert
import org.junit.Test

class WrapperTest {

    private val server = WrapperServer()

    @Test
    fun testSDK() {

        Assert.assertTrue(

            initLocalServer()
        )
        Assert.assertTrue(

            runTests()
        )
        Assert.assertTrue(

            terminateLocalServer()
        )
    }

    private fun initLocalServer(): Boolean {

        return server.init()
    }

    private fun runTests(): Boolean {

        return false
    }

    private fun terminateLocalServer(): Boolean {

        return server.shutdown()
    }
}