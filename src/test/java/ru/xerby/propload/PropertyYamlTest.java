package ru.xerby.propload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;

public class PropertyYamlTest {
    File file = Paths.get("test.yaml").toFile();

    @Test
    public void testLoadYaml() throws IOException {
        Assert.assertFalse("File must not exist", file.exists());
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.SPLIT_LINES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.writeValue(file, propertyDictionary);

        try {
            propertyDictionary.clear();

            propertyDictionary = PropertyDictionary.loadFromFile(file);
            Assert.assertEquals("Check that all properties were loaded", 12, propertyDictionary.size());

            PropertyDefinition dbPath = propertyDictionary.get("DB_PATH");
            Assert.assertTrue("Check name of parametrized property", dbPath.getName().equalsIgnoreCase("DB_PATH"));
            Assert.assertEquals("Check property description", "Path to database", dbPath.getDescription());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must have parameter",
                    PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, dbPath.getParametrization());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must be string",
                    PropertyDefinition.ParamType.STRING, dbPath.getParamType());
            Assert.assertTrue("Property which was created by createKeyValueRequiredProperty must be required", dbPath.isRequired());

            dbPath = propertyDictionary.get("DELAYED");
            Assert.assertTrue("Check name of parameterless property", dbPath.getName().equalsIgnoreCase("DELAYED"));
            Assert.assertEquals("Property which was created by createParameterlessProperty must not have parameter",
                    PropertyDefinition.ParametrizationDegree.PARAMETER_PROHIBITED, dbPath.getParametrization());
            Assert.assertNull("When property is parameterless, its paramType must be null", dbPath.getParamType());
            Assert.assertFalse("Property which was created by createParameterlessProperty is not necessary", dbPath.isRequired());

            dbPath = propertyDictionary.get("SERVER_URL");
            Assert.assertEquals("Property which was created by createKeyValueOptionalProperty must have parameter",
                    PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, dbPath.getParametrization());
            Assert.assertEquals("Property which was created by createKeyValueOptionalProperty must be string",
                    PropertyDefinition.ParamType.STRING, dbPath.getParamType());
            Assert.assertFalse("Property which was created by createKeyValueOptionalProperty is not necessary", dbPath.isRequired());
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @Test
    public void testLoadYamlWithoutPropertyNames() throws IOException {
        Assert.assertFalse("File must not exist", file.exists());
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.SPLIT_LINES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.writeValue(writer, propertyDictionary);
        String yamlContent = writer.toString();
        yamlContent = yamlContent.replaceAll("\\s\\sname:.*?\\n", "");
        Assert.assertFalse("Yaml must not contain property names", yamlContent.contains(" name:"));

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.print(yamlContent);
        }

        try {
            propertyDictionary.clear();

            try (InputStream inputStream = new FileInputStream(file)) {
                propertyDictionary = PropertyDictionary.loadFromInputStream(inputStream);
            }
            Assert.assertEquals("Check that all properties were loaded", 12, propertyDictionary.size());

            PropertyDefinition dbPath = propertyDictionary.get("DB_PATH");
            Assert.assertTrue("Check name of parametrized property", dbPath.getName().equalsIgnoreCase("DB_PATH"));
            Assert.assertEquals("Check property description", "Path to database", dbPath.getDescription());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must have parameter",
                    PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, dbPath.getParametrization());
            Assert.assertEquals("Property which was created by createKeyValueRequiredProperty must be string",
                    PropertyDefinition.ParamType.STRING, dbPath.getParamType());
            Assert.assertTrue("Property which was created by createKeyValueRequiredProperty must be required", dbPath.isRequired());

            dbPath = propertyDictionary.get("DELAYED");
            Assert.assertTrue("Check name of parameterless property", dbPath.getName().equalsIgnoreCase("DELAYED"));
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @Test
    public void loadResourceTest() {
        PropertyDictionary propertyDictionary = PropertyDictionary.loadFromResource("example.yaml");
        Assert.assertEquals("Check that all properties were loaded", 7, propertyDictionary.size());

        PropertyDefinition dbPath = propertyDictionary.get("DB_PATH");
        Assert.assertEquals(PropertyDefinition.ParametrizationDegree.PARAMETER_OPTIONAL, dbPath.getParametrization());
        Assert.assertEquals(PropertyDefinition.ParamType.STRING, dbPath.getParamType());

        PropertyDefinition delayTime = propertyDictionary.get("delayTime");
        Assert.assertEquals(PropertyDefinition.ParamType.INTEGER, delayTime.getParamType());
        Assert.assertEquals("60", delayTime.getDefaultValue());
        Assert.assertEquals(3, delayTime.getCmdAliases().length);
        Assert.assertEquals('l', delayTime.getCharCmdAlias());
        Assert.assertEquals(PropertyDefinition.ParametrizationDegree.PARAMETER_OPTIONAL, dbPath.getParametrization());

        PropertyDefinition user = propertyDictionary.get("MAIN_USERNAME");
        Assert.assertEquals("me", user.getDefaultValue());
        Assert.assertEquals(3, user.getCmdAliases().length);
        Assert.assertEquals(PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, user.getParametrization());
    }

    @Test
    public void loadBuggedResourceTest() {
        try {
            PropertyDictionary.loadFromResource("bugged_example.yaml", false);
            Assert.fail("Must throw exception due to name mismatch");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Property name must be almost the same as key, they can use different case or hyphens or dots, but alphanumerical characters must be equal (SERVER_PORT vs ClientPort)", e.getMessage());
        }
    }
}
