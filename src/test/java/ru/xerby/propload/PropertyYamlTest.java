package ru.xerby.propload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;

public class PropertyYamlTest {
    File file = Paths.get("test.yaml").toFile();

    @Test
    public void testLoadYaml() throws IOException {
        Assert.assertFalse("File must not exist", file.exists());
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.SPLIT_LINES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.writeValue(file, propertyRepository);

        try {
            propertyRepository.clear();

            propertyRepository = PropertyRepository.loadFromYamlFile(file);
            Assert.assertEquals("Check that all properties were loaded", 12, propertyRepository.size());

            PropertyDescription dbPath = propertyRepository.get("DB_PATH");
            Assert.assertTrue("Check name of parametrized property", dbPath.getName().equalsIgnoreCase("DB_PATH"));
            Assert.assertEquals("Check property description", "Path to database", dbPath.getDescription());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must have parameter",
                    PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, dbPath.getParametrization());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must be string",
                    PropertyDescription.ParamType.STRING, dbPath.getParamType());
            Assert.assertTrue("Property which was created by createKeyValueRequiredProperty must be required", dbPath.isRequired());

            dbPath = propertyRepository.get("DELAYED");
            Assert.assertTrue("Check name of parameterless property", dbPath.getName().equalsIgnoreCase("DELAYED"));
            Assert.assertEquals("Property which was created by createParameterlessProperty must not have parameter",
                    PropertyDescription.ParametrizationDegree.PARAMETER_PROHIBITED, dbPath.getParametrization());
            Assert.assertNull("When property is parameterless, its paramType must be null", dbPath.getParamType());
            Assert.assertFalse("Property which was created by createParameterlessProperty is not necessary", dbPath.isRequired());

            dbPath = propertyRepository.get("SERVER_URL");
            Assert.assertEquals("Property which was created by createKeyValueOptionalProperty must have parameter",
                    PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, dbPath.getParametrization());
            Assert.assertEquals("Property which was created by createKeyValueOptionalProperty must be string",
                    PropertyDescription.ParamType.STRING, dbPath.getParamType());
            Assert.assertFalse("Property which was created by createKeyValueOptionalProperty is not necessary", dbPath.isRequired());
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @Test
    public void testLoadYamlWithoutPropertyNames() throws IOException {
        Assert.assertFalse("File must not exist", file.exists());
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.SPLIT_LINES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.writeValue(writer, propertyRepository);
        String yamlContent = writer.toString();
        yamlContent = yamlContent.replaceAll("\\s\\sname:.*?\\n", "");
        Assert.assertFalse("Yaml must not contain property names", yamlContent.contains(" name:"));

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.print(yamlContent);
        }

        try {
            propertyRepository.clear();

            propertyRepository = PropertyRepository.loadFromYamlFile(file);
            Assert.assertEquals("Check that all properties were loaded", 12, propertyRepository.size());

            PropertyDescription dbPath = propertyRepository.get("DB_PATH");
            Assert.assertTrue("Check name of parametrized property", dbPath.getName().equalsIgnoreCase("DB_PATH"));
            Assert.assertEquals("Check property description", "Path to database", dbPath.getDescription());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must have parameter",
                    PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, dbPath.getParametrization());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must be string",
                    PropertyDescription.ParamType.STRING, dbPath.getParamType());
            Assert.assertTrue("Property which was created by createKeyValueRequiredProperty must be required", dbPath.isRequired());

            dbPath = propertyRepository.get("DELAYED");
            Assert.assertTrue("Check name of parameterless property", dbPath.getName().equalsIgnoreCase("DELAYED"));
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }
}
