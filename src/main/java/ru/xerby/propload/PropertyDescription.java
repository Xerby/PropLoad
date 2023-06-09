package ru.xerby.propload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PropertyDescription {
    private final String description;
    private final String defaultValue;//can't have defaultValue and be parameterless at the same time
    private final ParametrizationDegree parametrization;
    private final boolean isRequired; //can't be isRequired and hasDefaultValue at the same time
    private final ParamType paramType;
    private final String[] cmdAliases;
    private String name;

    public PropertyDescription(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("default_Value") String defaultValue,
            @JsonProperty("parametrized") ParametrizationDegree parametrized,
            @JsonProperty("required") boolean isRequired,
            @JsonProperty("param_type") ParamType paramType,
            @JsonProperty("cmd_aliases") String... cmdAliases) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.parametrization = parametrized;
        this.isRequired = isRequired;
        this.paramType = paramType;
        this.cmdAliases = cmdAliases;
        validate();
    }


    public PropertyDescription(String name, String description, String defaultValue, ParametrizationDegree parametrized, boolean isRequired, ParamType paramType) {
        this(name, description, defaultValue, parametrized, isRequired, paramType, null);
    }

    public static PropertyDescription createParameterlessProperty(String name, String description) {
        return new PropertyDescription(name, description, null, ParametrizationDegree.PARAMETER_PROHIBITED, false, null);
    }

    public static PropertyDescription createKeyValueRequiredProperty(String name, String description) {
        return new PropertyDescription(name, description, null, ParametrizationDegree.PARAMETER_REQUIRED, true, ParamType.STRING);
    }

    public static PropertyDescription createKeyValueOptionalProperty(String name, String description) {
        return new PropertyDescription(name, description, null, ParametrizationDegree.PARAMETER_REQUIRED, false, ParamType.STRING);
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

    //If PARAMETER_PROHIBITED, then you can't use property with parameter. If parameter will be used, then exception will be thrown.
    //If ONLY_EQUAL_SIGN_PARAMETER, then pair "key=value" will be treated as a parametrized key, but if you will use "key value",
    // "key" will be treated as a key without parameter and "value" will be ignored and be returned as remainder part.
    //If PARAMETER_OPTIONAL then either "key=value" or "key value" will be treated as a parametrized key, but you also can use "key" without parameter.
    //If PARAMETER_REQUIRED then if you don't use parameter, then exception will be thrown.
    public enum ParametrizationDegree {PARAMETER_PROHIBITED, ONLY_EQUALS_SIGN_PARAMETER, PARAMETER_OPTIONAL, PARAMETER_REQUIRED}

    public enum ParamType {BOOLEAN, STRING, INTEGER, FLOAT}
}
