package io.harness.cfsdk.cloud;

import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.harness.cfsdk.cloud.model.AuthInfo;

public class AuthResponseDecoder {

    private static final Logger log = LoggerFactory.getLogger(AuthResponseDecoder.class);

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

            log.warn("auth token decode failed: {}", e.getMessage(), e);
        }
        return null;
    }

    protected String[] splitToken(String token) {

        return token.split("[.]");
    }
}
