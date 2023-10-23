/*
 * Harness feature flag service client apis
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: cf@harness.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.harness.cfsdk.cloud.openapi.client.model;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.harness.cfsdk.cloud.openapi.client.model.TargetMap;
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

import io.harness.cfsdk.cloud.openapi.client.JSON;

/**
 * A mapping of variations to targets and target groups (segments).  The targets listed here should receive this variation.
 */

public class VariationMap {
  public static final String SERIALIZED_NAME_VARIATION = "variation";
  @SerializedName(SERIALIZED_NAME_VARIATION)
  private String variation;

  public static final String SERIALIZED_NAME_TARGETS = "targets";
  @SerializedName(SERIALIZED_NAME_TARGETS)
  private List<TargetMap> targets;

  public static final String SERIALIZED_NAME_TARGET_SEGMENTS = "targetSegments";
  @SerializedName(SERIALIZED_NAME_TARGET_SEGMENTS)
  private List<String> targetSegments;

  public VariationMap() {
  }

  public VariationMap variation(String variation) {
    
    this.variation = variation;
    return this;
  }

   /**
   * The variation identifier
   * @return variation
  **/
  
  public String getVariation() {
    return variation;
  }


  public void setVariation(String variation) {
    this.variation = variation;
  }


  public VariationMap targets(List<TargetMap> targets) {
    
    this.targets = targets;
    return this;
  }

  public VariationMap addTargetsItem(TargetMap targetsItem) {
    if (this.targets == null) {
      this.targets = new ArrayList<>();
    }
    this.targets.add(targetsItem);
    return this;
  }

   /**
   * A list of target mappings
   * @return targets
  **/
  
  public List<TargetMap> getTargets() {
    return targets;
  }


  public void setTargets(List<TargetMap> targets) {
    this.targets = targets;
  }


  public VariationMap targetSegments(List<String> targetSegments) {
    
    this.targetSegments = targetSegments;
    return this;
  }

  public VariationMap addTargetSegmentsItem(String targetSegmentsItem) {
    if (this.targetSegments == null) {
      this.targetSegments = new ArrayList<>();
    }
    this.targetSegments.add(targetSegmentsItem);
    return this;
  }

   /**
   * A list of target groups (segments)
   * @return targetSegments
  **/
  
  public List<String> getTargetSegments() {
    return targetSegments;
  }


  public void setTargetSegments(List<String> targetSegments) {
    this.targetSegments = targetSegments;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VariationMap variationMap = (VariationMap) o;
    return Objects.equals(this.variation, variationMap.variation) &&
        Objects.equals(this.targets, variationMap.targets) &&
        Objects.equals(this.targetSegments, variationMap.targetSegments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variation, targets, targetSegments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VariationMap {\n");
    sb.append("    variation: ").append(toIndentedString(variation)).append("\n");
    sb.append("    targets: ").append(toIndentedString(targets)).append("\n");
    sb.append("    targetSegments: ").append(toIndentedString(targetSegments)).append("\n");
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
    openapiFields.add("variation");
    openapiFields.add("targets");
    openapiFields.add("targetSegments");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("variation");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to VariationMap
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!VariationMap.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in VariationMap is not found in the empty JSON string", VariationMap.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!VariationMap.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `VariationMap` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : VariationMap.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("variation").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `variation` to be a primitive type in the JSON string but got `%s`", jsonObj.get("variation").toString()));
      }
      if (jsonObj.get("targets") != null && !jsonObj.get("targets").isJsonNull()) {
        JsonArray jsonArraytargets = jsonObj.getAsJsonArray("targets");
        if (jsonArraytargets != null) {
          // ensure the json data is an array
          if (!jsonObj.get("targets").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `targets` to be an array in the JSON string but got `%s`", jsonObj.get("targets").toString()));
          }

          // validate the optional field `targets` (array)
          for (int i = 0; i < jsonArraytargets.size(); i++) {
            TargetMap.validateJsonElement(jsonArraytargets.get(i));
          };
        }
      }
      // ensure the optional json data is an array if present
      if (jsonObj.get("targetSegments") != null && !jsonObj.get("targetSegments").isJsonNull() && !jsonObj.get("targetSegments").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `targetSegments` to be an array in the JSON string but got `%s`", jsonObj.get("targetSegments").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!VariationMap.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'VariationMap' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<VariationMap> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(VariationMap.class));

       return (TypeAdapter<T>) new TypeAdapter<VariationMap>() {
           @Override
           public void write(JsonWriter out, VariationMap value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public VariationMap read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of VariationMap given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of VariationMap
  * @throws IOException if the JSON string is invalid with respect to VariationMap
  */
  public static VariationMap fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, VariationMap.class);
  }

 /**
  * Convert an instance of VariationMap to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}
