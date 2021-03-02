package io.harness.cfsdk.cloud;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import com.google.gson.Gson;

import io.harness.cfsdk.cloud.model.AuthInfo;

public class AuthResponseDecoder {

    public @Nullable AuthInfo extractInfo(String token) {
        if (token == null) return null;
        try {
            String[] body = token.split("[.]");
            if (body.length > 2) {
                byte[] decoded = Base64.decode(body[1], Base64.DEFAULT);
                String decodeData = new String(decoded);
                Gson gson = new Gson();
                return gson.fromJson(decodeData, AuthInfo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
