package io.harness.cfsdk.testwrapper

import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.cache.CloudCache
import io.harness.cfsdk.cloud.events.AuthCallback
import io.harness.cfsdk.cloud.factories.CloudFactory
import io.harness.cfsdk.cloud.model.Target

class WrapperClient(cloudFactory: CloudFactory?) : CfClient(cloudFactory) {

    fun initialize(

        apiKey: String?,
        configuration: CfConfiguration?,
        target: Target?,
        cloudCache: CloudCache?,
        authCallback: AuthCallback?
    ) {

        doInitialize(

            apiKey,
            configuration,
            target,
            cloudCache,
            authCallback
        )
    }
}