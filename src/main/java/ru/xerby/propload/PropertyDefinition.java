package ru.xerby.propload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;


/**
 * In PropLoad, there are two crucial concepts: "property" and "property-definition." A "property" is the outcome provided by the library, which represents
 * a key-value pair. On the other hand, a "property-definition" is a structural element that defines the behavior and validation criteria for a property.
 *
 * <p>The library relies on a list of property definitions, known as "PropertyDictionary," to determine which properties should be available in the program
 * and how to handle situations when they are absent.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PropertyDefinition {
    private final String description;
    private final String defaultValue;//can't have defaultValue and be parameterless at the same time
    private final ParametrizationDegree parametrization;
    private final boolean isRequired; //can't be isRequired and hasDefaultValue at the same time
    private final ParamType paramType;
    private final String[] cmdAliases;
    private final char charCmdAlias;
    private String name;

    public PropertyDefinition(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("default_Value") String defaultValue,
            @JsonProperty("parametrized") ParametrizationDegree parametrized,
            @JsonProperty("required") boolean isRequired,
            @JsonProperty("param_type") ParamType paramType,
            @JsonProperty("char_cmd_alias") char charCmdAlias,
            @JsonProperty("cmd_aliases") String... cmdAliases) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.parametrization = parametrized;
        this.isRequired = isRequired;
        this.paramType = paramType;
        this.cmdAliases = cmdAliases;
        this.charCmdAlias = charCmdAlias;
        validate();
    }


    public PropertyDefinition(String name, String description, String defaultValue, ParametrizationDegree parametrized, boolean isRequired, ParamType paramType) {
        this(name, description, defaultValue, parametrized, isRequired, paramType, '\0', (String[]) null);
    }

    public static PropertyDefinition createParameterlessProperty(String name, String description) {
        return new PropertyDefinition(name, description, null, ParametrizationDegree.PARAMETER_PROHIBITED, false, null);
    }

    public static PropertyDefinition createKeyValueRequiredProperty(String name, String description) {
        return new PropertyDefinition(name, description, null, ParametrizationDegree.PARAMETER_REQUIRED, true, ParamType.STRING);
    }

    public static PropertyDefinition createKeyValueOptionalProperty(String name, String description) {
        return new PropertyDefinition(name, description, null, ParametrizationDegree.PARAMETER_REQUIRED, false, ParamType.STRING);
    }

    protected void validate() {
        if (defaultValue != null && parametrization == ParametrizationDegree.PARAMETER_PROHIBITED)
            throw new IllegalArgumentException("Can't set default value for parameterless property");

        if (isRequired && defaultValue != null)
            throw new IllegalArgumentException("Can't be isRequired and hasDefaultValue at the same time");
    }

    protected void setName(String name) {
        if (this.name != null)
            throw new IllegalArgumentException("It's prohibit to change name of property after it was created");

        this.name = name;
    }

    /**
     * Can or should a property have a parameter and how should it be designated
     * <p>If PARAMETER_PROHIBITED, then you can't use property with parameter. If parameter will be used, then exception will be thrown.
     * If PARAMETER_OPTIONAL then either "key=value" or "key value" will be treated as a parametrized key, but you also can use "key" without parameter.
     * If PARAMETER_REQUIRED then if you don't use parameter, then exception will be thrown.
     */
    public enum ParametrizationDegree {PARAMETER_PROHIBITED, PARAMETER_OPTIONAL, PARAMETER_REQUIRED}

    /**
     * What type of parameter can be used with a property. STRING by default
     */
    public enum ParamType {BOOLEAN, STRING, INTEGER, FLOAT}
}
