package io.harness.cfsdk.testwrapper.client

import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.events.AuthCallback
import io.harness.cfsdk.cloud.factories.CloudFactory
import io.harness.cfsdk.cloud.model.Target

class WrapperClient(cloudFactory: CloudFactory?) : CfClient(cloudFactory) {

    companion object {

        fun getInstance(): CfClient {

            if (instance == null) {
                synchronized(WrapperClient::class.java) {
                    if (instance == null) {

                        instance = WrapperClient(WrapperClientCloudFactory())
                    }
                }
            }
            return instance
        }
    }

    fun initialize(

        sdkKey: String?,
        configuration: CfConfiguration?,
        target: Target?,
        authCallback: AuthCallback?
    ) {

        setupNetworkInfo()
        doInitialize(

            sdkKey,
            configuration,
            target,
            TmpStorage(),
            authCallback
        )
    }

    private fun setupNetworkInfo() {

        if (networkInfoProvider != null) {

            networkInfoProvider.unregisterAll()
        } else {

            networkInfoProvider = WrapperClientNetworkInfoProvider()
        }
    }
}