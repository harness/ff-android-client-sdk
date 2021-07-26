package io.harness.cfsdk.testwrapper.context.api

import com.google.gson.annotations.SerializedName

data class FlagCheckRequestTarget(

    @SerializedName("target_identifier")
    val targetIdentifier: String,

    @SerializedName("target_name")
    val targetName: String
)