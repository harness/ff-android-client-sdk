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
import io.harness.cfsdk.cloud.openapi.client.model.FeatureState;
import io.harness.cfsdk.cloud.openapi.client.model.Prerequisite;
import io.harness.cfsdk.cloud.openapi.client.model.Serve;
import io.harness.cfsdk.cloud.openapi.client.model.ServingRule;
import io.harness.cfsdk.cloud.openapi.client.model.Variation;
import io.harness.cfsdk.cloud.openapi.client.model.VariationMap;
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
 * FeatureConfig
 */

public class FeatureConfig {
  public static final String SERIALIZED_NAME_PROJECT = "project";
  @SerializedName(SERIALIZED_NAME_PROJECT)
  private String project;

  public static final String SERIALIZED_NAME_ENVIRONMENT = "environment";
  @SerializedName(SERIALIZED_NAME_ENVIRONMENT)
  private String environment;

  public static final String SERIALIZED_NAME_FEATURE = "feature";
  @SerializedName(SERIALIZED_NAME_FEATURE)
  private String feature;

  public static final String SERIALIZED_NAME_STATE = "state";
  @SerializedName(SERIALIZED_NAME_STATE)
  private FeatureState state;

  /**
   * Gets or Sets kind
   */
  @JsonAdapter(KindEnum.Adapter.class)
  public enum KindEnum {
    BOOLEAN("boolean"),
    
    INT("int"),
    
    STRING("string"),
    
    JSON("json");

    private String value;

    KindEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static KindEnum fromValue(String value) {
      for (KindEnum b : KindEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<KindEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final KindEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public KindEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return KindEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_KIND = "kind";
  @SerializedName(SERIALIZED_NAME_KIND)
  private KindEnum kind;

  public static final String SERIALIZED_NAME_VARIATIONS = "variations";
  @SerializedName(SERIALIZED_NAME_VARIATIONS)
  private List<Variation> variations = new ArrayList<>();

  public static final String SERIALIZED_NAME_RULES = "rules";
  @SerializedName(SERIALIZED_NAME_RULES)
  private List<ServingRule> rules;

  public static final String SERIALIZED_NAME_DEFAULT_SERVE = "defaultServe";
  @SerializedName(SERIALIZED_NAME_DEFAULT_SERVE)
  private Serve defaultServe;

  public static final String SERIALIZED_NAME_OFF_VARIATION = "offVariation";
  @SerializedName(SERIALIZED_NAME_OFF_VARIATION)
  private String offVariation;

  public static final String SERIALIZED_NAME_PREREQUISITES = "prerequisites";
  @SerializedName(SERIALIZED_NAME_PREREQUISITES)
  private List<Prerequisite> prerequisites;

  public static final String SERIALIZED_NAME_VARIATION_TO_TARGET_MAP = "variationToTargetMap";
  @SerializedName(SERIALIZED_NAME_VARIATION_TO_TARGET_MAP)
  private List<VariationMap> variationToTargetMap;

  public static final String SERIALIZED_NAME_VERSION = "version";
  @SerializedName(SERIALIZED_NAME_VERSION)
  private Long version;

  public FeatureConfig() {
  }

  public FeatureConfig project(String project) {
    
    this.project = project;
    return this;
  }

   /**
   * Get project
   * @return project
  **/
  
  public String getProject() {
    return project;
  }


  public void setProject(String project) {
    this.project = project;
  }


  public FeatureConfig environment(String environment) {
    
    this.environment = environment;
    return this;
  }

   /**
   * Get environment
   * @return environment
  **/
  
  public String getEnvironment() {
    return environment;
  }


  public void setEnvironment(String environment) {
    this.environment = environment;
  }


  public FeatureConfig feature(String feature) {
    
    this.feature = feature;
    return this;
  }

   /**
   * Get feature
   * @return feature
  **/
  
  public String getFeature() {
    return feature;
  }


  public void setFeature(String feature) {
    this.feature = feature;
  }


  public FeatureConfig state(FeatureState state) {
    
    this.state = state;
    return this;
  }

   /**
   * Get state
   * @return state
  **/
  
  public FeatureState getState() {
    return state;
  }


  public void setState(FeatureState state) {
    this.state = state;
  }


  public FeatureConfig kind(KindEnum kind) {
    
    this.kind = kind;
    return this;
  }

   /**
   * Get kind
   * @return kind
  **/
  
  public KindEnum getKind() {
    return kind;
  }


  public void setKind(KindEnum kind) {
    this.kind = kind;
  }


  public FeatureConfig variations(List<Variation> variations) {
    
    this.variations = variations;
    return this;
  }

  public FeatureConfig addVariationsItem(Variation variationsItem) {
    if (this.variations == null) {
      this.variations = new ArrayList<>();
    }
    this.variations.add(variationsItem);
    return this;
  }

   /**
   * Get variations
   * @return variations
  **/
  
  public List<Variation> getVariations() {
    return variations;
  }


  public void setVariations(List<Variation> variations) {
    this.variations = variations;
  }


  public FeatureConfig rules(List<ServingRule> rules) {
    
    this.rules = rules;
    return this;
  }

  public FeatureConfig addRulesItem(ServingRule rulesItem) {
    if (this.rules == null) {
      this.rules = new ArrayList<>();
    }
    this.rules.add(rulesItem);
    return this;
  }

   /**
   * Get rules
   * @return rules
  **/
  
  public List<ServingRule> getRules() {
    return rules;
  }


  public void setRules(List<ServingRule> rules) {
    this.rules = rules;
  }


  public FeatureConfig defaultServe(Serve defaultServe) {
    
    this.defaultServe = defaultServe;
    return this;
  }

   /**
   * Get defaultServe
   * @return defaultServe
  **/
  
  public Serve getDefaultServe() {
    return defaultServe;
  }


  public void setDefaultServe(Serve defaultServe) {
    this.defaultServe = defaultServe;
  }


  public FeatureConfig offVariation(String offVariation) {
    
    this.offVariation = offVariation;
    return this;
  }

   /**
   * Get offVariation
   * @return offVariation
  **/
  
  public String getOffVariation() {
    return offVariation;
  }


  public void setOffVariation(String offVariation) {
    this.offVariation = offVariation;
  }


  public FeatureConfig prerequisites(List<Prerequisite> prerequisites) {
    
    this.prerequisites = prerequisites;
    return this;
  }

  public FeatureConfig addPrerequisitesItem(Prerequisite prerequisitesItem) {
    if (this.prerequisites == null) {
      this.prerequisites = new ArrayList<>();
    }
    this.prerequisites.add(prerequisitesItem);
    return this;
  }

   /**
   * Get prerequisites
   * @return prerequisites
  **/
  
  public List<Prerequisite> getPrerequisites() {
    return prerequisites;
  }


  public void setPrerequisites(List<Prerequisite> prerequisites) {
    this.prerequisites = prerequisites;
  }


  public FeatureConfig variationToTargetMap(List<VariationMap> variationToTargetMap) {
    
    this.variationToTargetMap = variationToTargetMap;
    return this;
  }

  public FeatureConfig addVariationToTargetMapItem(VariationMap variationToTargetMapItem) {
    if (this.variationToTargetMap == null) {
      this.variationToTargetMap = new ArrayList<>();
    }
    this.variationToTargetMap.add(variationToTargetMapItem);
    return this;
  }

   /**
   * Get variationToTargetMap
   * @return variationToTargetMap
  **/
  
  public List<VariationMap> getVariationToTargetMap() {
    return variationToTargetMap;
  }


  public void setVariationToTargetMap(List<VariationMap> variationToTargetMap) {
    this.variationToTargetMap = variationToTargetMap;
  }


  public FeatureConfig version(Long version) {
    
    this.version = version;
    return this;
  }

   /**
   * Get version
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
    FeatureConfig featureConfig = (FeatureConfig) o;
    return Objects.equals(this.project, featureConfig.project) &&
        Objects.equals(this.environment, featureConfig.environment) &&
        Objects.equals(this.feature, featureConfig.feature) &&
        Objects.equals(this.state, featureConfig.state) &&
        Objects.equals(this.kind, featureConfig.kind) &&
        Objects.equals(this.variations, featureConfig.variations) &&
        Objects.equals(this.rules, featureConfig.rules) &&
        Objects.equals(this.defaultServe, featureConfig.defaultServe) &&
        Objects.equals(this.offVariation, featureConfig.offVariation) &&
        Objects.equals(this.prerequisites, featureConfig.prerequisites) &&
        Objects.equals(this.variationToTargetMap, featureConfig.variationToTargetMap) &&
        Objects.equals(this.version, featureConfig.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(project, environment, feature, state, kind, variations, rules, defaultServe, offVariation, prerequisites, variationToTargetMap, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FeatureConfig {\n");
    sb.append("    project: ").append(toIndentedString(project)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    feature: ").append(toIndentedString(feature)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    variations: ").append(toIndentedString(variations)).append("\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    defaultServe: ").append(toIndentedString(defaultServe)).append("\n");
    sb.append("    offVariation: ").append(toIndentedString(offVariation)).append("\n");
    sb.append("    prerequisites: ").append(toIndentedString(prerequisites)).append("\n");
    sb.append("    variationToTargetMap: ").append(toIndentedString(variationToTargetMap)).append("\n");
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
    openapiFields.add("project");
    openapiFields.add("environment");
    openapiFields.add("feature");
    openapiFields.add("state");
    openapiFields.add("kind");
    openapiFields.add("variations");
    openapiFields.add("rules");
    openapiFields.add("defaultServe");
    openapiFields.add("offVariation");
    openapiFields.add("prerequisites");
    openapiFields.add("variationToTargetMap");
    openapiFields.add("version");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("project");
    openapiRequiredFields.add("environment");
    openapiRequiredFields.add("feature");
    openapiRequiredFields.add("state");
    openapiRequiredFields.add("kind");
    openapiRequiredFields.add("variations");
    openapiRequiredFields.add("defaultServe");
    openapiRequiredFields.add("offVariation");
  }

 /**
  * Validates the JSON Element and throws an exception if issues found
  *
  * @param jsonElement JSON Element
  * @throws IOException if the JSON Element is invalid with respect to FeatureConfig
  */
  public static void validateJsonElement(JsonElement jsonElement) throws IOException {
      if (jsonElement == null) {
        if (!FeatureConfig.openapiRequiredFields.isEmpty()) { // has required fields but JSON element is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in FeatureConfig is not found in the empty JSON string", FeatureConfig.openapiRequiredFields.toString()));
        }
      }

      Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
      // check to see if the JSON string contains additional fields
      for (Map.Entry<String, JsonElement> entry : entries) {
        if (!FeatureConfig.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `FeatureConfig` properties. JSON: %s", entry.getKey(), jsonElement.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : FeatureConfig.openapiRequiredFields) {
        if (jsonElement.getAsJsonObject().get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonElement.toString()));
        }
      }
        JsonObject jsonObj = jsonElement.getAsJsonObject();
      if (!jsonObj.get("project").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `project` to be a primitive type in the JSON string but got `%s`", jsonObj.get("project").toString()));
      }
      if (!jsonObj.get("environment").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `environment` to be a primitive type in the JSON string but got `%s`", jsonObj.get("environment").toString()));
      }
      if (!jsonObj.get("feature").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `feature` to be a primitive type in the JSON string but got `%s`", jsonObj.get("feature").toString()));
      }
      if (!jsonObj.get("kind").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `kind` to be a primitive type in the JSON string but got `%s`", jsonObj.get("kind").toString()));
      }
      // ensure the json data is an array
      if (!jsonObj.get("variations").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `variations` to be an array in the JSON string but got `%s`", jsonObj.get("variations").toString()));
      }

      JsonArray jsonArrayvariations = jsonObj.getAsJsonArray("variations");
      // validate the required field `variations` (array)
      for (int i = 0; i < jsonArrayvariations.size(); i++) {
        Variation.validateJsonElement(jsonArrayvariations.get(i));
      };
      if (jsonObj.get("rules") != null && !jsonObj.get("rules").isJsonNull()) {
        JsonArray jsonArrayrules = jsonObj.getAsJsonArray("rules");
        if (jsonArrayrules != null) {
          // ensure the json data is an array
          if (!jsonObj.get("rules").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `rules` to be an array in the JSON string but got `%s`", jsonObj.get("rules").toString()));
          }

          // validate the optional field `rules` (array)
          for (int i = 0; i < jsonArrayrules.size(); i++) {
            ServingRule.validateJsonElement(jsonArrayrules.get(i));
          };
        }
      }
      // validate the required field `defaultServe`
      Serve.validateJsonElement(jsonObj.get("defaultServe"));
      if (!jsonObj.get("offVariation").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `offVariation` to be a primitive type in the JSON string but got `%s`", jsonObj.get("offVariation").toString()));
      }
      if (jsonObj.get("prerequisites") != null && !jsonObj.get("prerequisites").isJsonNull()) {
        JsonArray jsonArrayprerequisites = jsonObj.getAsJsonArray("prerequisites");
        if (jsonArrayprerequisites != null) {
          // ensure the json data is an array
          if (!jsonObj.get("prerequisites").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `prerequisites` to be an array in the JSON string but got `%s`", jsonObj.get("prerequisites").toString()));
          }

          // validate the optional field `prerequisites` (array)
          for (int i = 0; i < jsonArrayprerequisites.size(); i++) {
            Prerequisite.validateJsonElement(jsonArrayprerequisites.get(i));
          };
        }
      }
      if (jsonObj.get("variationToTargetMap") != null && !jsonObj.get("variationToTargetMap").isJsonNull()) {
        JsonArray jsonArrayvariationToTargetMap = jsonObj.getAsJsonArray("variationToTargetMap");
        if (jsonArrayvariationToTargetMap != null) {
          // ensure the json data is an array
          if (!jsonObj.get("variationToTargetMap").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `variationToTargetMap` to be an array in the JSON string but got `%s`", jsonObj.get("variationToTargetMap").toString()));
          }

          // validate the optional field `variationToTargetMap` (array)
          for (int i = 0; i < jsonArrayvariationToTargetMap.size(); i++) {
            VariationMap.validateJsonElement(jsonArrayvariationToTargetMap.get(i));
          };
        }
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!FeatureConfig.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'FeatureConfig' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<FeatureConfig> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(FeatureConfig.class));

       return (TypeAdapter<T>) new TypeAdapter<FeatureConfig>() {
           @Override
           public void write(JsonWriter out, FeatureConfig value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public FeatureConfig read(JsonReader in) throws IOException {
             JsonElement jsonElement = elementAdapter.read(in);
             validateJsonElement(jsonElement);
             return thisAdapter.fromJsonTree(jsonElement);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of FeatureConfig given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of FeatureConfig
  * @throws IOException if the JSON string is invalid with respect to FeatureConfig
  */
  public static FeatureConfig fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, FeatureConfig.class);
  }

 /**
  * Convert an instance of FeatureConfig to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

