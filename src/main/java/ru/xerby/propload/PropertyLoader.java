package ru.xerby.propload;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Data
public class PropertyLoader {

    private static final String DEFAULT_INNER_PROPERTY_FILE_NAME = "properties.properties";
    @Getter(AccessLevel.NONE)
    private final PropertyRepository propertyRepository;
    private final Map<Object, String> properties = new HashMap<>();

    private boolean isEnabledWindowsKeyCompatibility = false;
    private boolean throwExceptionIfUnboundTokenFound = true;
    private boolean isParametrizedWithoutEqualSignAllowed = true;
    private boolean throwExceptionIfUnknownCmdPropertyFound = true;

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

    public void loadFromEnvironment() {
        loadFromEnvironment(null);
    }

    public void loadFromEnvironment(String envPropertyPrefix) {
        loadFromProperties(System.getenv(), envPropertyPrefix, throwExceptionIfUnknownEnvPropertyFound && envPropertyPrefix != null && !envPropertyPrefix.isEmpty());
    }

    @SneakyThrows
    public void loadFromFile(File file) {
        Properties loadedProperties = new Properties();
        loadedProperties.load(file.toURI().toURL().openStream());
        loadFromProperties(loadedProperties, null, throwExceptionIfUnknownPropFilePropertyFound);
    }

    private boolean isContainKey(String key) {
        if (caseSensitive)
            return properties.containsKey(key);
        else
            return properties.keySet().stream().anyMatch(k -> k.toString().equalsIgnoreCase(key));
    }

    private boolean isContainKey(PropertyRepository.PossiblyCaseInsensitiveString key) {
        if (caseSensitive)
            return properties.containsKey(key);
        else
            return properties.keySet().stream().anyMatch(k -> k.toString().equalsIgnoreCase(key.getValue()));
    }

    protected void loadFromProperties(Map<?, ?> externalProperties, String prefix, boolean throwExceptionIfUnknownPropertyFound) {
        if (externalProperties == null)
            return;

        for (Object pName : externalProperties.keySet()) {
            String fullPropName = (String) pName;
            if (prefix != null && !prefix.isEmpty() && !(fullPropName.startsWith(prefix)))
                continue;

            String propName;
            if (prefix == null || prefix.isEmpty())
                propName = fullPropName;
            else
                propName = fullPropName.substring(prefix.length());

            if (isContainKey(propName))
                continue;

            PropertyDescription propertyDescription = propertyRepository.get(propName);
            if (propertyDescription == null)
                if (throwExceptionIfUnknownPropertyFound)
                    throw new IllegalArgumentException("Unknown property \"" + propName + "\" was found in environment" +
                            (prefix == null ? null : " (prefix \"" + prefix + "\")"));
                else
                    continue;

            checkValueType(propName, (String) externalProperties.get(fullPropName), propertyDescription.getParamType());

            String propValue;
            if (propertyDescription.getParamType() == null)
                propValue = null;
            else
                propValue = (String) externalProperties.get(fullPropName);
            properties.put(propName, propValue);
        }
    }

    public void setDefaultIfIsNotSet() {
        for (PropertyRepository.PossiblyCaseInsensitiveString propName : propertyRepository.keySet()) {
            if (isContainKey(propName))
                continue;

            PropertyDescription propertyDescription = propertyRepository.get(propName);
            if (propertyDescription.getDefaultValue() == null && propertyDescription.isRequired())
                throw new IllegalArgumentException("Property " + propName + " is required, but it's not set");
            else if (propertyDescription.getDefaultValue() != null)
                properties.put(propName.getValue(), propertyDescription.getDefaultValue());
        }
    }

    public void buildProperties(String[] commandLineArgs,
                                String outerFilePath,
                                String envPropertyPrefix,
                                String resourceName) {
        loadFromCmdArgs(commandLineArgs);

        if (outerFilePath != null)
            loadFromFile(Paths.get(outerFilePath).toFile());

        loadFromEnvironment(envPropertyPrefix);

        URL url;
        if (resourceName != null) {
            url = getClass().getClassLoader().getResource(resourceName);
            if (url == null)
                throw new IllegalArgumentException("Resource " + resourceName + " not found");
            File file = new File(url.getFile());
            if (file.exists())
                loadFromFile(file);
            else
                throw new IllegalArgumentException("Resource " + resourceName + " not found");
        } else {
            url = getClass().getClassLoader().getResource(DEFAULT_INNER_PROPERTY_FILE_NAME);
            if (url != null)
                loadFromFile(new File(url.getFile()));
        }

        setDefaultIfIsNotSet();
    }


    @SuppressWarnings({"ResultOfMethodCallIgnored", "java:S2201"})
    protected void checkValueType(String propName, String propValue, PropertyDescription.ParamType paramType) {
        if (propValue == null) return;
        if (paramType == null)
            if ((propValue.isEmpty() || propValue.equals(" ")) || propValue.equals("true") || propValue.equals("t") || propValue.equals("yes") || propValue.equals("1") || propValue.equals("y"))
                return;
            else
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
                        && !token.equals("yes") && !token.equals("no") && !token.equals("y") && !token.equals("n")
                        && !token.equals("0") && !token.equals("1") && !token.equals("-1"))
                    throw new IllegalArgumentException("Unknown boolean value " + propValue + " for property " + propName);
                break;
            case FLOAT:
                Double.parseDouble(propValue);
                break;
//            default:
//                throw new IllegalArgumentException("Unknown param type " + paramType + " for property " + propName);
        }
    }
}
