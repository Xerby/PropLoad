package ru.xerby.propload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;

import java.io.File;
import java.util.HashMap;

public class PropertyRepository extends HashMap<String, PropertyDescription> {
    @SneakyThrows
    @SuppressWarnings("java:S2864")
    public static PropertyRepository loadFromYamlFile(File yaml) {
        PropertyRepository propertyRepository = new ObjectMapper(new YAMLFactory()).readValue(yaml, PropertyRepository.class);

        for (String key : propertyRepository.keySet()) {
            if (propertyRepository.get(key).getName() == null)
                propertyRepository.get(key).setName(key);
        }
        return propertyRepository;
    }

    public void registerProperty(PropertyDescription value) {
        this.put(value.getName(), value);
    }
}
