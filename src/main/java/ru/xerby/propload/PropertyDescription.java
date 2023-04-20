package ru.xerby.propload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PropertyDescription {
    private String name;
    private String description;
    private String defaultValue;
    private ParametrizationDegree parametrization;
    private boolean isRequired;
    private ParamType paramType;
    private String[] cmdAliases;

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


    //If PARAMETER_PROHIBITED, then you can't use property with parameter. If parameter will be used, then exception will be thrown.
    //If ONLY_EQUAL_SIGN_PARAMETER, then pair "key=value" will be treated as a parametrized key, but if you will use "key value",
    // "key" will be treated as a key without parameter and "value" will be ignored and be returned as remainder part.
    //If PARAMETER_OPTIONAL then either "key=value" or "key value" will be treated as a parametrized key, but you also can use "key" without parameter.
    //If PARAMETER_REQUIRED then if you don't use parameter, then exception will be thrown.
    public enum ParametrizationDegree {PARAMETER_PROHIBITED, ONLY_EQUALS_SIGN_PARAMETER, PARAMETER_OPTIONAL, PARAMETER_REQUIRED}

    public enum ParamType {BOOLEAN, STRING, INTEGER, FLOAT}
}
