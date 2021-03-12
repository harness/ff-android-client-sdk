package io.harness.cfsdk.cloud.model;

public class Target {
    private String name;
    private String identifier;

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
}
