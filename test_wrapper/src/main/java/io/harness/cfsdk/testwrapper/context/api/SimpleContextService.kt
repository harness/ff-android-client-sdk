package io.harness.cfsdk.testwrapper.context.api

import io.harness.cfsdk.testwrapper.context.SimpleContextFactory
import retrofit2.Call
import retrofit2.http.GET

interface SimpleContextService {

    @GET(SimpleContextFactory.PATH_VERSION)
    fun version(): Call<VersionResponse>
}