package ru.xerby.propload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.util.HashMap;

@NoArgsConstructor
public class PropertyRepository extends HashMap<PropertyRepository.PossiblyCaseInsensitiveString, PropertyDescription> {

    private boolean isCaseSensitive;

    public static PropertyRepository loadFromYamlFile(File yaml) {
        return loadFromYamlFile(yaml, false);
    }

    @SneakyThrows
    @SuppressWarnings("java:S2864")
    public static PropertyRepository loadFromYamlFile(File yaml, boolean isCaseSensitive) {
        TempPropertyRepository tempPropertyRepository = new ObjectMapper(new YAMLFactory()).readValue(yaml, TempPropertyRepository.class);

        PropertyRepository propertyRepository = new PropertyRepository();
        propertyRepository.isCaseSensitive = isCaseSensitive;

        for (String key : tempPropertyRepository.keySet()) {
            String realKey;
            if (tempPropertyRepository.get(key).getName() == null) {
                realKey = key;
                tempPropertyRepository.get(key).setName(realKey);
            } else
                realKey = tempPropertyRepository.get(key).getName();


            propertyRepository.put(realKey, tempPropertyRepository.get(key));
        }
        return propertyRepository;
    }

    public void put(String key, PropertyDescription value) {
        this.put(new PossiblyCaseInsensitiveString(key), value);
    }

    public PropertyDescription get(String key) {
        return this.get(new PossiblyCaseInsensitiveString(key));
    }

    public void registerProperty(PropertyDescription value) {
        this.put(new PossiblyCaseInsensitiveString(value.getName()), value);
    }

    public static class TempPropertyRepository extends HashMap<String, PropertyDescription> {
    }

    public class PossiblyCaseInsensitiveString {

        private final String value;

        public PossiblyCaseInsensitiveString(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PossiblyCaseInsensitiveString that = (PossiblyCaseInsensitiveString) o;
            if (isCaseSensitive)
                return value.equals(that.value);
            else
                return value.equalsIgnoreCase(that.value);
        }

        @Override
        public int hashCode() {
            if (value == null)
                return 0;
            if (isCaseSensitive)
                return value.hashCode();
            else
                return value.toLowerCase().hashCode();
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
