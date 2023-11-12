package ru.xerby.propload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;

import java.io.File;
import java.io.InputStream;
import java.util.TreeMap;


/**
 * The class is responsible for loading and storing definitions of properties that must or may be present.
 * Any work with properties should start with this class.
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

        adjustNames(propertyDictionary);
        return propertyDictionary;
    }

    @SneakyThrows
    @SuppressWarnings("java:S2864")
    public static PropertyDictionary loadFromFile(File file, boolean caseSensitive) {
        PropertyDictionary propertyDictionary = new ObjectMapper(new YAMLFactory()).readValue(file, PropertyDictionary.class);

        adjustNames(propertyDictionary);
        return propertyDictionary;
    }


    private static void adjustNames(PropertyDictionary propertyDictionary) {
        for (String key : propertyDictionary.keySet()) {
            if (propertyDictionary.get(key).getName() == null)
                propertyDictionary.get(key).setName(key);
        }
    }

    public void registerProperty(PropertyDefinition value) {
        this.put(value.getName(), value);
    }

    public static PropertyDictionary loadFromResource(String fileName, boolean caseSensitive) {
        return loadFromInputStream(PropertyDictionary.class.getClassLoader().getResourceAsStream(fileName), caseSensitive);
    }
}
