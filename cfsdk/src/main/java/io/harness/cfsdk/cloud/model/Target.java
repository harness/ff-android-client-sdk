package io.harness.cfsdk.cloud.model;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Target {

    private String name;
    private String identifier;
    private Map<String, Object> attributes;
    private boolean isPrivate; // If the target is private
    private Set<String> privateAttributes; // Custom set to set the attributes which are private

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

        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(identifier);
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
