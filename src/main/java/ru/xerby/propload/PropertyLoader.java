package ru.xerby.propload;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * This class is responsible for loading properties from different sources. Properties that should or can be loaded
 * are listed in the {@link PropertyRepository}; if the property is not in the repository, then it will not be loaded,
 * even if it is present in one of the sources.
 * <p>If the same property is present in different sources, then preference is given to higher priority sources.
 * The command line has maximum priority, then the external settings file, then the environment and then
 * the internal resource file. If the property is not found in any of the sources, but it has a default value,
 * then it is entered.
 * <p>By default, the user can specify an external settings file by specifying the path to it on the command line
 * (property-file property) or in environment variables, if a prefix is specified for environment variables.
 * <p>The class also has many settings that allow you to process sources differently and validate them with varying degrees of strictness.
 * Is it possible to specify parameterized properties without an equal sign, is it possible to specify properties in the Windows way,
 * will an exception throw if an unknown command line parameter is encountered. The only parameter that is not set in this class is case sensitivity.
 * It must be specified in {@link PropertyRepository}.
 */
@Data
@Slf4j
public class PropertyLoader {

    private static final String DEFAULT_INNER_PROPERTY_FILE_NAME = "properties.properties";
    private static final String REDEFINED_PROPERTY_FILE_PROPERTY_NAME = "property-file";
    @Getter(AccessLevel.NONE)
    private final PropertyRepository propertyRepository;
    private final Map<String, String> properties;

    /**
     * If true, then the user can specify an external settings file by specifying the path to it on the command line (key: property-file) or in environment variables.
     * True by default.
     */
    private boolean canRedefineExternalPropertyFile = true;

    /**
     * If true, then the user can specify properties in the Windows way: /key value or /key=value. False by default.
     */
    private boolean isEnabledWindowsKeyCompatibility = false;

    /**
     * If true, then an exception will be thrown if a value is found that does not belong to any property.  If false, then such values will be ignored.
     * For example, \"-key1=value1 -key2 value2 value3 -key5\". \"value2\" will be considered as a value for \"key2\" and \"key3\" will be considered as dangling token.
     * If true, then an exception will be thrown, if false, then \"key3\" will be ignored. True by default.
     */
    private boolean throwExceptionIfUnboundTokenFound = true;

    /**
     * If true, then the user can specify parameterized properties without an equal sign, for example, \"-key value\".
     * If false, then such properties must use equal sign, for example, \"-key=value\". True by default.
     */
    private boolean isParametrizedWithoutEqualSignAllowed = true;

    /**
     * If true, then an exception will be thrown if an unknown command line parameter is encountered. If false, then such parameters will be ignored.
     * True by default.
     */
    private boolean throwExceptionIfUnknownCmdPropertyFound = true;

    /**
     * If true, then an exception will be thrown if an unknown environment variable is encountered. If false, then such variables will be ignored.
     * The parameter is checked only if the environment variable prefix is specified, otherwise it is ignored
     * (because there will be many unfamiliar environment variables anyway).
     * True by default.
     */
    private boolean throwExceptionIfUnknownEnvPropertyFound = true;

    /**
     * If true, then an exception will be thrown if an unknown property is encountered in the external settings file.
     * If false, then such properties will be ignored. False by default.
     */
    private boolean throwExceptionIfUnknownPropFilePropertyFound = false;

    /**
     * If true, then an exception will be thrown if the resource file is not found. If false, then such properties will be ignored.
     * True by default.
     */
    private boolean throwExceptionIfPropertyResourceNotFound = true;

    @Setter(AccessLevel.NONE)
    private boolean caseSensitive;

    public PropertyLoader(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
        caseSensitive = propertyRepository.caseSensitive;
        this.properties = new TreeMap<>(caseSensitive ? String::compareTo : String::compareToIgnoreCase);
    }

    public void loadFromCmdArgs(String[] args) {
        ParsedCmdProperties parsedCmdProperties = ParsedCmdProperties.parse(args, isEnabledWindowsKeyCompatibility, throwExceptionIfUnboundTokenFound);
        for (ParsedCmdProperty parsedCmdProperty : parsedCmdProperties) {
            PropertyDefinition propertyDefinition = propertyRepository.get(parsedCmdProperty.getKey());
            if (propertyDefinition == null)
                if (throwExceptionIfUnknownCmdPropertyFound)
                    throw new IllegalArgumentException("Unknown property \"" + parsedCmdProperty.getKey() + "\" was found in command line arguments");
                else
                    continue;

            if (parsedCmdProperty.isSurelyParametrized() || isParametrizedWithoutEqualSignAllowed) {
                checkValueType(parsedCmdProperty.getKey(), parsedCmdProperty.getValue(), propertyDefinition.getParamType());
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
        try (var stream = file.toURI().toURL().openStream()) {
            loadedProperties.load(stream);
        }
        loadFromProperties(loadedProperties, null, throwExceptionIfUnknownPropFilePropertyFound);
    }

    @SneakyThrows
    public void loadFromStream(InputStream stream) {
        Properties loadedProperties = new Properties();
        loadedProperties.load(stream);
        loadFromProperties(loadedProperties, null, throwExceptionIfUnknownPropFilePropertyFound);
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

            if (properties.containsKey(propName))
                continue;

            PropertyDefinition propertyDefinition = propertyRepository.get(propName);
            if (propertyDefinition == null)
                if (throwExceptionIfUnknownPropertyFound)
                    throw new IllegalArgumentException("Unknown property \"" + propName + "\" was found in environment" +
                            (prefix == null ? null : " (prefix \"" + prefix + "\")"));
                else
                    continue;

            checkValueType(propName, (String) externalProperties.get(fullPropName), propertyDefinition.getParamType());

            String propValue;
            if (propertyDefinition.getParamType() == null)
                propValue = null;
            else
                propValue = (String) externalProperties.get(fullPropName);
            properties.put(propName, propValue);
        }
    }

    public void setDefaultIfIsNotSet() {
        for (String propName : propertyRepository.keySet()) {
            if (properties.containsKey(propName))
                continue;

            PropertyDefinition propertyDefinition = propertyRepository.get(propName);
            if (propertyDefinition.getDefaultValue() == null && propertyDefinition.isRequired())
                throw new IllegalArgumentException("Property " + propName + " is required, but it's not set");
            else if (propertyDefinition.getDefaultValue() != null)
                properties.put(propName, propertyDefinition.getDefaultValue());
        }
    }

    public void buildProperties(String[] commandLineArgs,
                                String externalPropertyFilePath,
                                String envPropertyPrefix,
                                String resourceName) {
        if (canRedefineExternalPropertyFile)
            propertyRepository.registerProperty(new PropertyDefinition(REDEFINED_PROPERTY_FILE_PROPERTY_NAME, "Path to external properties file",
                    null, PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.STRING));

        properties.clear();
        loadFromCmdArgs(commandLineArgs);

        externalPropertyFilePath = getExternalPropertyFilePath(externalPropertyFilePath, envPropertyPrefix);

        if (externalPropertyFilePath != null)
            loadFromFile(Paths.get(externalPropertyFilePath).toFile());

        loadFromEnvironment(envPropertyPrefix);

        properties.remove(REDEFINED_PROPERTY_FILE_PROPERTY_NAME);

        InputStream resource;
        if (resourceName != null) {
            resource = getClass().getClassLoader().getResourceAsStream(resourceName);
            if (resource == null && throwExceptionIfPropertyResourceNotFound)
                throw new IllegalArgumentException("Resource " + resourceName + " not found");
            if (resource != null)
                loadFromStream(resource);
        } else {
            resource = getClass().getClassLoader().getResourceAsStream(DEFAULT_INNER_PROPERTY_FILE_NAME);
            if (resource != null)
                loadFromStream(resource);
        }

        setDefaultIfIsNotSet();
    }

    protected String getExternalPropertyFilePath(String originalExternalPropertyFilePath, String envPropertyPrefix) {
        if (!canRedefineExternalPropertyFile)
            return originalExternalPropertyFilePath;

        if (!properties.containsKey(REDEFINED_PROPERTY_FILE_PROPERTY_NAME) && envPropertyPrefix != null) {
            String envProp = System.getenv(envPropertyPrefix + REDEFINED_PROPERTY_FILE_PROPERTY_NAME);
            if (envProp != null) {
                log.debug("Property " + envPropertyPrefix + REDEFINED_PROPERTY_FILE_PROPERTY_NAME + " was found in environment variables");
                properties.put(REDEFINED_PROPERTY_FILE_PROPERTY_NAME, envProp);
            }
        }

        String externalPropertyFilePath = null;
        if (properties.containsKey(REDEFINED_PROPERTY_FILE_PROPERTY_NAME)) {
            externalPropertyFilePath = properties.get(REDEFINED_PROPERTY_FILE_PROPERTY_NAME);
            log.debug("property-file path was overridden to " + externalPropertyFilePath);
        } else {
            if (envPropertyPrefix != null)
                externalPropertyFilePath = System.getenv(envPropertyPrefix + REDEFINED_PROPERTY_FILE_PROPERTY_NAME);
        }

        return externalPropertyFilePath == null ? originalExternalPropertyFilePath : externalPropertyFilePath;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "java:S2201"})
    protected void checkValueType(String propName, String propValue, PropertyDefinition.ParamType paramType) {
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
                getAsBoolean(propValue, propName);
                break;
            case FLOAT:
                Double.parseDouble(propValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown param type " + paramType + " for property " + propName);
        }
    }

    public boolean getAsBoolean(String key) {
        return getAsBoolean(properties.get(key), key);
    }

    private boolean getAsBoolean(String propValue, String keyForLogging) {
        if (propValue == null)
            return false;
        else {
            String value = propValue.strip().toLowerCase();
            if (value.equals("true") || value.equals("t") || value.equals("yes") || value.equals("1") || value.equals("y"))
                return true;
            else if (value.equals("false") || value.equals("f") || value.equals("no") || value.equals("0") || value.equals("n"))
                return false;
            else
                throw new IllegalArgumentException("Unknown boolean value " + value + " for property " + keyForLogging);
        }
    }
}
