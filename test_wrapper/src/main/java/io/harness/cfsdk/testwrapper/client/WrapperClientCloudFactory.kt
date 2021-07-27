package io.harness.cfsdk.testwrapper.client

import io.harness.cfsdk.cloud.AuthResponseDecoder
import io.harness.cfsdk.cloud.factories.CloudFactory

class WrapperClientCloudFactory : CloudFactory() {

    override fun getAuthResponseDecoder(): AuthResponseDecoder {

        return WrapperClientAuthResponseDecoder()
    }
}