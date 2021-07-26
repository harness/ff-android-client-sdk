package io.harness.cfsdk.testwrapper.context.api

import io.harness.cfsdk.testwrapper.context.ApiContextFactory
import retrofit2.Call
import retrofit2.http.GET

interface ApiContextService {

    @GET(ApiContextFactory.PATH_PING)
    fun ping(): Call<PongResponse>


}