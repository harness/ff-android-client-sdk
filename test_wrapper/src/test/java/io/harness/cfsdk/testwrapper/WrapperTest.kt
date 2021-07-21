package io.harness.cfsdk.testwrapper

import org.junit.Assert
import org.junit.Test

class WrapperTest {

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

        return false
    }

    private fun runTests(): Boolean {

        return false
    }

    private fun terminateLocalServer(): Boolean {

        return false
    }
}