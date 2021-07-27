package io.harness.cfsdk.testwrapper.client

import androidx.annotation.Nullable
import com.google.gson.Gson
import io.harness.cfsdk.cloud.AuthResponseDecoder
import io.harness.cfsdk.cloud.model.AuthInfo
import io.harness.cfsdk.logging.CfLog
import java.util.*

class WrapperClientAuthResponseDecoder : AuthResponseDecoder() {

    private val tag = WrapperClientAuthResponseDecoder::class.simpleName

    @Nullable
    override fun extractInfo(token: String?): AuthInfo? {

        val decoder = Base64.getDecoder()

        if (token == null) {

            return null
        }

        try {

            val body = splitToken(token)
            if (body.size > 2) {

                val decoded = decoder.decode(body[1])
                val decodeData = String(decoded)

                CfLog.OUT.v(tag, "Decoded data: $decodeData")

                val authInfo = Gson().fromJson(decodeData, AuthInfo::class.java)

                CfLog.OUT.i(tag, "$authInfo")
                return authInfo
            }
        } catch (e: Exception) {

            CfLog.OUT.e(tag, e.message, e)
        }

        return null
    }
}