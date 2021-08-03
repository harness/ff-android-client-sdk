package io.harness.cfsdk.testwrapper.client

import io.harness.cfsdk.cloud.network.NetworkInfoProviding

class WrapperClientNetworkInfoProvider : NetworkInfoProviding() {

    override fun isNetworkAvailable() = true
}