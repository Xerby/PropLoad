package ru.xerby.propload;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Data
public class PropertyLoader {

    @Getter(AccessLevel.NONE)
    private final PropertyRepository propertyRepository;
    private final Map<Object, String> properties = new HashMap<>();

    private boolean isEnabledWindowsKeyCompatibility = false;
    private boolean throwExceptionIfUnboundTokenFound = true;
    private boolean isParametrizedWithoutEqualSignAllowed = true;
    private boolean throwExceptionIfUnknownCmdPropertyFound = true;

    private String envPropertyPrefix = null;
    private boolean throwExceptionIfUnknownEnvPropertyFound = true; //works only if envPropertyPrefix is set
    private boolean throwExceptionIfUnknownPropFilePropertyFound = false;
    private boolean caseSensitive = false;

    public void loadFromCmdArgs(String[] args) {
        ParsedCmdProperties parsedCmdProperties = ParsedCmdProperties.parse(args, isEnabledWindowsKeyCompatibility, throwExceptionIfUnboundTokenFound);
        for (ParsedCmdProperty parsedCmdProperty : parsedCmdProperties) {
            PropertyDescription propertyDescription = propertyRepository.get(parsedCmdProperty.getKey());
            if (propertyDescription == null)
                if (throwExceptionIfUnknownCmdPropertyFound)
                    throw new IllegalArgumentException("Unknown property " + parsedCmdProperty.getKey() + " was found in command line arguments");
                else
                    continue;

            if (parsedCmdProperty.isSurelyParametrized() || isParametrizedWithoutEqualSignAllowed) {
                checkValueType(parsedCmdProperty.getKey(), parsedCmdProperty.getValue(), propertyDescription.getParamType());
                properties.put(parsedCmdProperty.getKey(), parsedCmdProperty.getValue());
            } else if (parsedCmdProperty.getValue() == null)
                properties.put(parsedCmdProperty.getKey(), null);
            else
                throw new IllegalArgumentException("Property " + parsedCmdProperty.getKey() + " is parametrized without equal sign, but it's prohibited");
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "java:S2201"})
    protected void checkValueType(String propName, String propValue, PropertyDescription.ParamType paramType) {
        if (propValue == null) return;
        if (paramType == null)
            throw new IllegalArgumentException("Property " + propName + " is not parametrized, but it's value is " + propValue);

        switch (paramType) {
            case STRING:
                break;
            case INTEGER:
                Integer.parseInt(propValue);
                break;
            case BOOLEAN:
                String token = propValue.strip().toLowerCase();
                if (!token.equals("true") && !token.equals("false") && !token.equals("t") && !token.equals("f")
                        && !token.equals("yes") && !token.equals("no") && !token.equals("y") && !token.equals("n"))
                    throw new IllegalArgumentException("Unknown boolean value " + propValue + " for property " + propName);
                break;
            case FLOAT:
                Double.parseDouble(propValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown param type " + paramType + " for property " + propName);
        }
    }
}
