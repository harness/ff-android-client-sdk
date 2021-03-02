package io.harness.cfsdk.cloud;

import java.util.HashMap;

public class TokenProvider {
    private HashMap<String, String> tokenMap = new HashMap<>();

    String getToken(String key) {
        return tokenMap.get(key);
    }

    void addToken(String key, String token) {
        tokenMap.put(key, token);
    }
}
