package io.harness.cfsdk.cloud;

import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.logging.CfLog;

public class AuthResponseDecoder {

    private final String logTag;

    {

        logTag = AuthResponseDecoder.class.getSimpleName();
    }

    public @Nullable
    AuthInfo extractInfo(String token) {

        if (token == null) {

            return null;
        }
        try {

            String[] body = splitToken(token);
            if (body.length > 2) {

                byte[] decoded = Base64.decode(body[1], Base64.DEFAULT);
                String decodeData = new String(decoded);
                Gson gson = new Gson();
                return gson.fromJson(decodeData, AuthInfo.class);
            }
        } catch (Exception e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
        return null;
    }

    protected String[] splitToken(String token) {

        return token.split("[.]");
    }
}
