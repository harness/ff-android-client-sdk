package io.harness.cfsdk.cloud.model;


import java.util.HashMap;
import java.util.Map;

import io.harness.cfsdk.utils.CfUtils;

public class Target {

    private String name;
    private String identifier;
    private final Map<String, Object> attributes = new HashMap<>();

    public String getName() {

        return name;
    }

    public Target name(String name) {

        this.name = name;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Target identifier(String identifier) {

        this.identifier = identifier;
        return this;
    }

    public boolean isValid() {

        return CfUtils.Text.isNotEmpty(identifier);
    }

    public Map<String, Object> getAttributes() {

        return attributes;
    }
}
