/*
 * Harness feature flag analytics service
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: cf@harness.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package io.harness.cfsdk.cloud.analytics.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * TargetData
 */
public class TargetData {

    public static final String SERIALIZED_NAME_IDENTIFIER = "identifier";
    @SerializedName(SERIALIZED_NAME_IDENTIFIER)
    private String identifier;

    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName(SERIALIZED_NAME_NAME)
    private String name;

    public static final String SERIALIZED_NAME_ATTRIBUTES = "attributes";
    @SerializedName(SERIALIZED_NAME_ATTRIBUTES)
    private List<KeyValue> attributes = new ArrayList<>();


    public TargetData identifier(String identifier) {

        this.identifier = identifier;
        return this;
    }

    /**
     * Get identifier
     *
     * @return identifier
     **/
    @ApiModelProperty(required = true, value = "")

    public String getIdentifier() {
        return identifier;
    }


    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    public TargetData name(String name) {

        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @ApiModelProperty(required = true, value = "")

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public TargetData attributes(List<KeyValue> attributes) {

        this.attributes = attributes;
        return this;
    }

    public TargetData addAttributesItem(KeyValue attributesItem) {
        this.attributes.add(attributesItem);
        return this;
    }

    /**
     * Get attributes
     *
     * @return attributes
     **/
    @ApiModelProperty(required = true, value = "")

    public List<KeyValue> getAttributes() {
        return attributes;
    }


    public void setAttributes(List<KeyValue> attributes) {
        this.attributes = attributes;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TargetData targetData = (TargetData) o;
        return Objects.equals(this.identifier, targetData.identifier) &&
                Objects.equals(this.name, targetData.name) &&
                Objects.equals(this.attributes, targetData.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, name, attributes);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TargetData {\n");
        sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}