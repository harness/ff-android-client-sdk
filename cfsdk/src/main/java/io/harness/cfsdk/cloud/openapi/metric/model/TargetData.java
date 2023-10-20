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


package io.harness.cfsdk.cloud.openapi.metric.model;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.harness.cfsdk.cloud.openapi.metric.model.KeyValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.harness.cfsdk.cloud.openapi.metric.JSON;

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

  public TargetData() {
  }

  public TargetData identifier(String identifier) {
    
    this.identifier = identifier;
    return this;
  }

   /**
   * Get identifier
   * @return identifier
  **/
  
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
   * @return name
  **/
  
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
    if (this.attributes == null) {
      this.attributes = new ArrayList<>();
    }
    this.attributes.add(attributesItem);
    return this;
  }

   /**
   * Get attributes
   * @return attributes
  **/
  
  public List<KeyValue> getAttributes() {
    return attributes;
  }


  public void setAttributes(List<KeyValue> attributes) {
    this.attributes = attributes;
  }



  @Override
  public boolean equals(Object o) {
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


  public static HashSet<String> openapiFields;
  public static HashSet<String> openapiRequiredFields;

  static {
    // a set of all properties/fields (JSON key names)
    openapiFields = new HashSet<String>();
    openapiFields.add("identifier");
    openapiFields.add("name");
    openapiFields.add("attributes");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("identifier");
    openapiRequiredFields.add("name");
    openapiRequiredFields.add("attributes");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to TargetData
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!TargetData.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in TargetData is not found in the empty JSON string", TargetData.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!TargetData.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `TargetData` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : TargetData.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("identifier").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `identifier` to be a primitive type in the JSON string but got `%s`", jsonObj.get("identifier").toString()));
      }
      if (!jsonObj.get("name").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `name` to be a primitive type in the JSON string but got `%s`", jsonObj.get("name").toString()));
      }
      // ensure the json data is an array
      if (!jsonObj.get("attributes").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `attributes` to be an array in the JSON string but got `%s`", jsonObj.get("attributes").toString()));
      }

      JsonArray jsonArrayattributes = jsonObj.getAsJsonArray("attributes");
      // validate the required field `attributes` (array)
      for (int i = 0; i < jsonArrayattributes.size(); i++) {
        KeyValue.validateJsonElement(jsonArrayattributes.get(i));
      };
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!TargetData.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'TargetData' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<TargetData> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(TargetData.class));

       return (TypeAdapter<T>) new TypeAdapter<TargetData>() {
           @Override
           public void write(JsonWriter out, TargetData value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public TargetData read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of TargetData given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of TargetData
  * @throws IOException if the JSON string is invalid with respect to TargetData
  */
  public static TargetData fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, TargetData.class);
  }

 /**
  * Convert an instance of TargetData to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

