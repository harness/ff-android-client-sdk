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
 * MetricsData
 */

public class MetricsData {
  public static final String SERIALIZED_NAME_TIMESTAMP = "timestamp";
  @SerializedName(SERIALIZED_NAME_TIMESTAMP)
  private Long timestamp;

  public static final String SERIALIZED_NAME_COUNT = "count";
  @SerializedName(SERIALIZED_NAME_COUNT)
  private Integer count;

  /**
   * This can be of type FeatureMetrics
   */
  @JsonAdapter(MetricsTypeEnum.Adapter.class)
  public enum MetricsTypeEnum {
    FFMETRICS("FFMETRICS");

    private String value;

    MetricsTypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static MetricsTypeEnum fromValue(String value) {
      for (MetricsTypeEnum b : MetricsTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<MetricsTypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final MetricsTypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public MetricsTypeEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return MetricsTypeEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_METRICS_TYPE = "metricsType";
  @SerializedName(SERIALIZED_NAME_METRICS_TYPE)
  private MetricsTypeEnum metricsType;

  public static final String SERIALIZED_NAME_ATTRIBUTES = "attributes";
  @SerializedName(SERIALIZED_NAME_ATTRIBUTES)
  private List<KeyValue> attributes = new ArrayList<>();

  public MetricsData() {
  }

  public MetricsData timestamp(Long timestamp) {
    
    this.timestamp = timestamp;
    return this;
  }

   /**
   * time at when this data was recorded
   * @return timestamp
  **/
  
  public Long getTimestamp() {
    return timestamp;
  }


  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }


  public MetricsData count(Integer count) {
    
    this.count = count;
    return this;
  }

   /**
   * Get count
   * @return count
  **/
  
  public Integer getCount() {
    return count;
  }


  public void setCount(Integer count) {
    this.count = count;
  }


  public MetricsData metricsType(MetricsTypeEnum metricsType) {
    
    this.metricsType = metricsType;
    return this;
  }

   /**
   * This can be of type FeatureMetrics
   * @return metricsType
  **/
  
  public MetricsTypeEnum getMetricsType() {
    return metricsType;
  }


  public void setMetricsType(MetricsTypeEnum metricsType) {
    this.metricsType = metricsType;
  }


  public MetricsData attributes(List<KeyValue> attributes) {
    
    this.attributes = attributes;
    return this;
  }

  public MetricsData addAttributesItem(KeyValue attributesItem) {
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
    MetricsData metricsData = (MetricsData) o;
    return Objects.equals(this.timestamp, metricsData.timestamp) &&
        Objects.equals(this.count, metricsData.count) &&
        Objects.equals(this.metricsType, metricsData.metricsType) &&
        Objects.equals(this.attributes, metricsData.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, count, metricsType, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MetricsData {\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    metricsType: ").append(toIndentedString(metricsType)).append("\n");
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
    openapiFields.add("timestamp");
    openapiFields.add("count");
    openapiFields.add("metricsType");
    openapiFields.add("attributes");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("timestamp");
    openapiRequiredFields.add("count");
    openapiRequiredFields.add("metricsType");
    openapiRequiredFields.add("attributes");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to MetricsData
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!MetricsData.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in MetricsData is not found in the empty JSON string", MetricsData.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!MetricsData.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `MetricsData` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : MetricsData.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("metricsType").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `metricsType` to be a primitive type in the JSON string but got `%s`", jsonObj.get("metricsType").toString()));
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
       if (!MetricsData.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'MetricsData' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<MetricsData> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(MetricsData.class));

       return (TypeAdapter<T>) new TypeAdapter<MetricsData>() {
           @Override
           public void write(JsonWriter out, MetricsData value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public MetricsData read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of MetricsData given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of MetricsData
  * @throws IOException if the JSON string is invalid with respect to MetricsData
  */
  public static MetricsData fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, MetricsData.class);
  }

 /**
  * Convert an instance of MetricsData to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

