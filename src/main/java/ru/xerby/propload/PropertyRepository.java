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
 * The most straightforward way to load properties is to use the static method loadFromYamlFile.
 */
@SuppressWarnings("java:S2160")
public class PropertyRepository extends TreeMap<String, PropertyDefinition> {

    public final boolean caseSensitive;

    public PropertyRepository(boolean caseSensitive) {
        super(caseSensitive ? String::compareTo : String::compareToIgnoreCase);
        this.caseSensitive = caseSensitive;
    }

    public PropertyRepository() {
        this(false);
    }

    public static PropertyRepository loadFromYamlFile(File yaml) {
        return loadFromYamlFile(yaml, false);
    }

    public static PropertyRepository loadFromInputStream(InputStream yaml) {
        return loadFromInputStream(yaml, false);
    }

    @SneakyThrows
    public static PropertyRepository loadFromInputStream(InputStream yaml, boolean caseSensitive) {
        PropertyRepository propertyRepository = new ObjectMapper(new YAMLFactory()).readValue(yaml, PropertyRepository.class);

        adjustNames(propertyRepository);
        return propertyRepository;
    }

    @SneakyThrows
    @SuppressWarnings("java:S2864")
    public static PropertyRepository loadFromYamlFile(File yaml, boolean caseSensitive) {
        PropertyRepository propertyRepository = new ObjectMapper(new YAMLFactory()).readValue(yaml, PropertyRepository.class);

        adjustNames(propertyRepository);
        return propertyRepository;
    }

    private static void adjustNames(PropertyRepository propertyRepository) {
        for (String key : propertyRepository.keySet()) {
            if (propertyRepository.get(key).getName() == null)
                propertyRepository.get(key).setName(key);
        }
    }

    public void registerProperty(PropertyDefinition value) {
        this.put(value.getName(), value);
    }
}
