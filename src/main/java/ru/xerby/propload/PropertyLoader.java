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
                properties.put(parsedCmdProperty.getKey(), parsedCmdProperty.getValue());
            } else if (parsedCmdProperty.getValue() == null)
                properties.put(parsedCmdProperty.getKey(), null);
            else
                throw new IllegalArgumentException("Property " + parsedCmdProperty.getKey() + " is parametrized without equal sign, but it's prohibited");
        }
    }
}
