package ru.xerby.propload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * The class is responsible for loading and storing definitions of properties that must or may be present.
 * Any work with properties should start with this class.
 * The dictionary is used to determine which properties should be available in the program and how to handle
 * situations when they are absent.
 * Properties are case-insensitive by default, but it can be changed using a one-parameter constructor.
 * The most straightforward way to load properties is to use the static method loadFromFile or loadFromResource.
 */
@SuppressWarnings("java:S2160")
public class PropertyDictionary extends TreeMap<String, PropertyDefinition> {
    public final boolean caseSensitive;

    public PropertyDictionary(boolean caseSensitive) {
        super(caseSensitive ? String::compareTo : String::compareToIgnoreCase);
        this.caseSensitive = caseSensitive;
    }

    public static PropertyDictionary loadFromResource(String fileName) {
        return loadFromInputStream(PropertyDictionary.class.getClassLoader().getResourceAsStream(fileName), false);
    }

    private boolean areKeysEqual(String o1, String o2) {
        return caseSensitive ? o1.equals(o2) : o1.equalsIgnoreCase(o2);
    }

    public PropertyDictionary() {
        this(false);
    }

    public static PropertyDictionary loadFromFile(File file) {
        return loadFromFile(file, false);
    }

    public static PropertyDictionary loadFromInputStream(InputStream stream) {
        return loadFromInputStream(stream, false);
    }

    @SneakyThrows
    public static PropertyDictionary loadFromInputStream(InputStream stream, boolean caseSensitive) {
        PropertyDictionary propertyDictionary = new ObjectMapper(new YAMLFactory()).readValue(stream, PropertyDictionary.class);

        propertyDictionary.adjustNames();
        return propertyDictionary;
    }

    @SneakyThrows
    @SuppressWarnings("java:S2864")
    public static PropertyDictionary loadFromFile(File file, boolean caseSensitive) {
        PropertyDictionary propertyDictionary = new ObjectMapper(new YAMLFactory()).readValue(file, PropertyDictionary.class);

        propertyDictionary.adjustNames();
        return propertyDictionary;
    }

    private boolean areKeysEqual(char o1, char o2) {
        return caseSensitive ? o1 == o2 : Character.toLowerCase(o1) == Character.toLowerCase(o2);
    }

    public void registerProperty(PropertyDefinition value) {
        this.put(value.getName(), value);
    }

    @Synchronized
    private void adjustNames() {
        Map<String, String> changedProperties = null;
        for (Map.Entry<String, PropertyDefinition> e : entrySet()) {
            if (e.getValue() == null)
                put(e.getKey(), new PropertyDefinition(e.getKey(), null, null, null, false, null));
            else if (e.getValue().getName() == null || e.getValue().getName().isEmpty())
                e.getValue().setName(e.getKey());
            else if (!areKeysEqual(e.getValue().getName(), e.getKey())) {
                if (!e.getKey().replaceAll("[^A-Za-z0-9]", "").equalsIgnoreCase(e.getValue().getName().replaceAll("[^A-Za-z0-9]", "")))
                    throw new IllegalArgumentException("Property name must be almost the same as key, they can use different case or hyphens or dots, but alphanumerical characters must be equal (" + e.getKey() + " vs " + e.getValue().getName() + ")");
                if (changedProperties == null)
                    changedProperties = new HashMap<>();
                changedProperties.put(e.getKey(), e.getValue().getName());
            }
        }
        if (changedProperties != null)
            for (Map.Entry<String, String> e : changedProperties.entrySet()) {
                put(e.getValue(), get(e.getKey()));
                remove(e.getKey());
            }
    }

    public static PropertyDictionary loadFromResource(String fileName, boolean caseSensitive) {
        return loadFromInputStream(PropertyDictionary.class.getClassLoader().getResourceAsStream(fileName), caseSensitive);
    }

    public PropertyDefinition getByCmdProperty(ParsedCmdProperty prop) {
        for (PropertyDefinition propertyDefinition : this.values()) {
            if (isKeyForProperty(prop, propertyDefinition))
                return propertyDefinition;
        }

        return null;
    }

    private boolean isKeyForProperty(ParsedCmdProperty prop, PropertyDefinition propertyDefinition) {
        if (areKeysEqual(propertyDefinition.getName(), prop.getLongKey()))
            return true;
        if (prop.getShortKey() != '\0' && areKeysEqual(propertyDefinition.getCharCmdAlias(), prop.getShortKey()))
            return true;
        if (propertyDefinition.getCmdAliases() != null)
            for (String cmdAlias : propertyDefinition.getCmdAliases()) {
                if (areKeysEqual(cmdAlias, prop.getLongKey()))
                    return true;
            }

        return false;
    }
}
