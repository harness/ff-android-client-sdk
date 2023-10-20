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
import io.harness.cfsdk.cloud.openapi.client.model.Clause;
import io.harness.cfsdk.cloud.openapi.client.model.Serve;
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
 * The rule used to determine what variation to serve to a target
 */

public class ServingRule {
  public static final String SERIALIZED_NAME_RULE_ID = "ruleId";
  @SerializedName(SERIALIZED_NAME_RULE_ID)
  private String ruleId;

  public static final String SERIALIZED_NAME_PRIORITY = "priority";
  @SerializedName(SERIALIZED_NAME_PRIORITY)
  private Integer priority;

  public static final String SERIALIZED_NAME_CLAUSES = "clauses";
  @SerializedName(SERIALIZED_NAME_CLAUSES)
  private List<Clause> clauses = new ArrayList<>();

  public static final String SERIALIZED_NAME_SERVE = "serve";
  @SerializedName(SERIALIZED_NAME_SERVE)
  private Serve serve;

  public ServingRule() {
  }

  public ServingRule ruleId(String ruleId) {
    
    this.ruleId = ruleId;
    return this;
  }

   /**
   * The unique identifier for this rule
   * @return ruleId
  **/
  
  public String getRuleId() {
    return ruleId;
  }


  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }


  public ServingRule priority(Integer priority) {
    
    this.priority = priority;
    return this;
  }

   /**
   * The rules priority relative to other rules.  The rules are evaluated in order with 1 being the highest
   * @return priority
  **/
  
  public Integer getPriority() {
    return priority;
  }


  public void setPriority(Integer priority) {
    this.priority = priority;
  }


  public ServingRule clauses(List<Clause> clauses) {
    
    this.clauses = clauses;
    return this;
  }

  public ServingRule addClausesItem(Clause clausesItem) {
    if (this.clauses == null) {
      this.clauses = new ArrayList<>();
    }
    this.clauses.add(clausesItem);
    return this;
  }

   /**
   * A list of clauses to use in the rule
   * @return clauses
  **/
  
  public List<Clause> getClauses() {
    return clauses;
  }


  public void setClauses(List<Clause> clauses) {
    this.clauses = clauses;
  }


  public ServingRule serve(Serve serve) {
    
    this.serve = serve;
    return this;
  }

   /**
   * Get serve
   * @return serve
  **/
  
  public Serve getServe() {
    return serve;
  }


  public void setServe(Serve serve) {
    this.serve = serve;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServingRule servingRule = (ServingRule) o;
    return Objects.equals(this.ruleId, servingRule.ruleId) &&
        Objects.equals(this.priority, servingRule.priority) &&
        Objects.equals(this.clauses, servingRule.clauses) &&
        Objects.equals(this.serve, servingRule.serve);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleId, priority, clauses, serve);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServingRule {\n");
    sb.append("    ruleId: ").append(toIndentedString(ruleId)).append("\n");
    sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
    sb.append("    clauses: ").append(toIndentedString(clauses)).append("\n");
    sb.append("    serve: ").append(toIndentedString(serve)).append("\n");
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
    openapiFields.add("ruleId");
    openapiFields.add("priority");
    openapiFields.add("clauses");
    openapiFields.add("serve");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("priority");
    openapiRequiredFields.add("clauses");
    openapiRequiredFields.add("serve");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to ServingRule
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!ServingRule.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in ServingRule is not found in the empty JSON string", ServingRule.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!ServingRule.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `ServingRule` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : ServingRule.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if ((jsonObj.get("ruleId") != null && !jsonObj.get("ruleId").isJsonNull()) && !jsonObj.get("ruleId").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `ruleId` to be a primitive type in the JSON string but got `%s`", jsonObj.get("ruleId").toString()));
      }
      // ensure the json data is an array
      if (!jsonObj.get("clauses").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `clauses` to be an array in the JSON string but got `%s`", jsonObj.get("clauses").toString()));
      }

      JsonArray jsonArrayclauses = jsonObj.getAsJsonArray("clauses");
      // validate the required field `clauses` (array)
      for (int i = 0; i < jsonArrayclauses.size(); i++) {
        Clause.validateJsonElement(jsonArrayclauses.get(i));
      };
      // validate the required field `serve`
      Serve.validateJsonElement(jsonObj.get("serve"));
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!ServingRule.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'ServingRule' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<ServingRule> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(ServingRule.class));

       return (TypeAdapter<T>) new TypeAdapter<ServingRule>() {
           @Override
           public void write(JsonWriter out, ServingRule value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public ServingRule read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of ServingRule given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of ServingRule
  * @throws IOException if the JSON string is invalid with respect to ServingRule
  */
  public static ServingRule fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, ServingRule.class);
  }

 /**
  * Convert an instance of ServingRule to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

