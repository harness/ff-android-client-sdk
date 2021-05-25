package io.harness.cfsdk.cloud.model;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.harness.cfsdk.utils.CfUtils;

public class Target {

    private String name;
    private String identifier;
    private boolean isPrivate; // If the target is private
    private Set<String> privateAttributes; // Custom set to set the attributes which are private
    private final Map<String, Object> attributes;

    {

        attributes = new HashMap<>();
    }

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

    public boolean isPrivate() {

        return isPrivate;
    }

    public Set<String> getPrivateAttributes() {

        return privateAttributes;
    }
}
