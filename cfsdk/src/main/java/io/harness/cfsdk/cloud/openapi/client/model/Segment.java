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
import io.harness.cfsdk.cloud.openapi.client.model.Tag;
import io.harness.cfsdk.cloud.openapi.client.model.Target;
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
 * A Target Group (Segment) response
 */

public class Segment {
  public static final String SERIALIZED_NAME_IDENTIFIER = "identifier";
  @SerializedName(SERIALIZED_NAME_IDENTIFIER)
  private String identifier;

  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_ENVIRONMENT = "environment";
  @SerializedName(SERIALIZED_NAME_ENVIRONMENT)
  private String environment;

  public static final String SERIALIZED_NAME_TAGS = "tags";
  @SerializedName(SERIALIZED_NAME_TAGS)
  private List<Tag> tags;

  public static final String SERIALIZED_NAME_INCLUDED = "included";
  @SerializedName(SERIALIZED_NAME_INCLUDED)
  private List<Target> included;

  public static final String SERIALIZED_NAME_EXCLUDED = "excluded";
  @SerializedName(SERIALIZED_NAME_EXCLUDED)
  private List<Target> excluded;

  public static final String SERIALIZED_NAME_RULES = "rules";
  @SerializedName(SERIALIZED_NAME_RULES)
  private List<Clause> rules;

  public static final String SERIALIZED_NAME_CREATED_AT = "createdAt";
  @SerializedName(SERIALIZED_NAME_CREATED_AT)
  private Long createdAt;

  public static final String SERIALIZED_NAME_MODIFIED_AT = "modifiedAt";
  @SerializedName(SERIALIZED_NAME_MODIFIED_AT)
  private Long modifiedAt;

  public static final String SERIALIZED_NAME_VERSION = "version";
  @SerializedName(SERIALIZED_NAME_VERSION)
  private Long version;

  public Segment() {
  }

  public Segment identifier(String identifier) {
    
    this.identifier = identifier;
    return this;
  }

   /**
   * Unique identifier for the target group.
   * @return identifier
  **/
  
  public String getIdentifier() {
    return identifier;
  }


  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }


  public Segment name(String name) {
    
    this.name = name;
    return this;
  }

   /**
   * Name of the target group.
   * @return name
  **/
  
  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public Segment environment(String environment) {
    
    this.environment = environment;
    return this;
  }

   /**
   * The environment this target group belongs to
   * @return environment
  **/
  
  public String getEnvironment() {
    return environment;
  }


  public void setEnvironment(String environment) {
    this.environment = environment;
  }


  public Segment tags(List<Tag> tags) {
    
    this.tags = tags;
    return this;
  }

  public Segment addTagsItem(Tag tagsItem) {
    if (this.tags == null) {
      this.tags = new ArrayList<>();
    }
    this.tags.add(tagsItem);
    return this;
  }

   /**
   * Tags for this target group
   * @return tags
  **/
  
  public List<Tag> getTags() {
    return tags;
  }


  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }


  public Segment included(List<Target> included) {
    
    this.included = included;
    return this;
  }

  public Segment addIncludedItem(Target includedItem) {
    if (this.included == null) {
      this.included = new ArrayList<>();
    }
    this.included.add(includedItem);
    return this;
  }

   /**
   * A list of Targets who belong to this target group
   * @return included
  **/
  
  public List<Target> getIncluded() {
    return included;
  }


  public void setIncluded(List<Target> included) {
    this.included = included;
  }


  public Segment excluded(List<Target> excluded) {
    
    this.excluded = excluded;
    return this;
  }

  public Segment addExcludedItem(Target excludedItem) {
    if (this.excluded == null) {
      this.excluded = new ArrayList<>();
    }
    this.excluded.add(excludedItem);
    return this;
  }

   /**
   * A list of Targets who are excluded from this target group
   * @return excluded
  **/
  
  public List<Target> getExcluded() {
    return excluded;
  }


  public void setExcluded(List<Target> excluded) {
    this.excluded = excluded;
  }


  public Segment rules(List<Clause> rules) {
    
    this.rules = rules;
    return this;
  }

  public Segment addRulesItem(Clause rulesItem) {
    if (this.rules == null) {
      this.rules = new ArrayList<>();
    }
    this.rules.add(rulesItem);
    return this;
  }

   /**
   * An array of rules that can cause a user to be included in this segment.
   * @return rules
  **/
  
  public List<Clause> getRules() {
    return rules;
  }


  public void setRules(List<Clause> rules) {
    this.rules = rules;
  }


  public Segment createdAt(Long createdAt) {
    
    this.createdAt = createdAt;
    return this;
  }

   /**
   * The data and time in milliseconds when the group was created
   * @return createdAt
  **/
  
  public Long getCreatedAt() {
    return createdAt;
  }


  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }


  public Segment modifiedAt(Long modifiedAt) {
    
    this.modifiedAt = modifiedAt;
    return this;
  }

   /**
   * The data and time in milliseconds when the group was last modified
   * @return modifiedAt
  **/
  
  public Long getModifiedAt() {
    return modifiedAt;
  }


  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }


  public Segment version(Long version) {
    
    this.version = version;
    return this;
  }

   /**
   * The version of this group.  Each time it is modified the version is incremented
   * @return version
  **/
  
  public Long getVersion() {
    return version;
  }


  public void setVersion(Long version) {
    this.version = version;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Segment segment = (Segment) o;
    return Objects.equals(this.identifier, segment.identifier) &&
        Objects.equals(this.name, segment.name) &&
        Objects.equals(this.environment, segment.environment) &&
        Objects.equals(this.tags, segment.tags) &&
        Objects.equals(this.included, segment.included) &&
        Objects.equals(this.excluded, segment.excluded) &&
        Objects.equals(this.rules, segment.rules) &&
        Objects.equals(this.createdAt, segment.createdAt) &&
        Objects.equals(this.modifiedAt, segment.modifiedAt) &&
        Objects.equals(this.version, segment.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, name, environment, tags, included, excluded, rules, createdAt, modifiedAt, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Segment {\n");
    sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    included: ").append(toIndentedString(included)).append("\n");
    sb.append("    excluded: ").append(toIndentedString(excluded)).append("\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    modifiedAt: ").append(toIndentedString(modifiedAt)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
    openapiFields.add("environment");
    openapiFields.add("tags");
    openapiFields.add("included");
    openapiFields.add("excluded");
    openapiFields.add("rules");
    openapiFields.add("createdAt");
    openapiFields.add("modifiedAt");
    openapiFields.add("version");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("identifier");
    openapiRequiredFields.add("name");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to Segment
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!Segment.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in Segment is not found in the empty JSON string", Segment.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!Segment.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `Segment` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : Segment.openapiRequiredFields) {
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
      if ((jsonObj.get("environment") != null && !jsonObj.get("environment").isJsonNull()) && !jsonObj.get("environment").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `environment` to be a primitive type in the JSON string but got `%s`", jsonObj.get("environment").toString()));
      }
      if (jsonObj.get("tags") != null && !jsonObj.get("tags").isJsonNull()) {
        JsonArray jsonArraytags = jsonObj.getAsJsonArray("tags");
        if (jsonArraytags != null) {
          // ensure the json data is an array
          if (!jsonObj.get("tags").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `tags` to be an array in the JSON string but got `%s`", jsonObj.get("tags").toString()));
          }

          // validate the optional field `tags` (array)
          for (int i = 0; i < jsonArraytags.size(); i++) {
            Tag.validateJsonElement(jsonArraytags.get(i));
          };
        }
      }
      if (jsonObj.get("included") != null && !jsonObj.get("included").isJsonNull()) {
        JsonArray jsonArrayincluded = jsonObj.getAsJsonArray("included");
        if (jsonArrayincluded != null) {
          // ensure the json data is an array
          if (!jsonObj.get("included").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `included` to be an array in the JSON string but got `%s`", jsonObj.get("included").toString()));
          }

          // validate the optional field `included` (array)
          for (int i = 0; i < jsonArrayincluded.size(); i++) {
            Target.validateJsonElement(jsonArrayincluded.get(i));
          };
        }
      }
      if (jsonObj.get("excluded") != null && !jsonObj.get("excluded").isJsonNull()) {
        JsonArray jsonArrayexcluded = jsonObj.getAsJsonArray("excluded");
        if (jsonArrayexcluded != null) {
          // ensure the json data is an array
          if (!jsonObj.get("excluded").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `excluded` to be an array in the JSON string but got `%s`", jsonObj.get("excluded").toString()));
          }

          // validate the optional field `excluded` (array)
          for (int i = 0; i < jsonArrayexcluded.size(); i++) {
            Target.validateJsonElement(jsonArrayexcluded.get(i));
          };
        }
      }
      if (jsonObj.get("rules") != null && !jsonObj.get("rules").isJsonNull()) {
        JsonArray jsonArrayrules = jsonObj.getAsJsonArray("rules");
        if (jsonArrayrules != null) {
          // ensure the json data is an array
          if (!jsonObj.get("rules").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `rules` to be an array in the JSON string but got `%s`", jsonObj.get("rules").toString()));
          }

          // validate the optional field `rules` (array)
          for (int i = 0; i < jsonArrayrules.size(); i++) {
            Clause.validateJsonElement(jsonArrayrules.get(i));
          };
        }
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!Segment.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'Segment' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<Segment> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(Segment.class));

       return (TypeAdapter<T>) new TypeAdapter<Segment>() {
           @Override
           public void write(JsonWriter out, Segment value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public Segment read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of Segment given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of Segment
  * @throws IOException if the JSON string is invalid with respect to Segment
  */
  public static Segment fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, Segment.class);
  }

 /**
  * Convert an instance of Segment to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

