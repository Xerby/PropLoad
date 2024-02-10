package ru.xerby.propload;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class PropertyLoaderTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void loadFromEnvironmentEmptyTest() {
        //we use long and bizarre names for properties to check that we don't accidentally affect real environmental variables
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyRepositoryWithLongNames();

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        propertyLoader.loadFromEnvironment();
        Assert.assertEquals("Check that we don't load any property, because environmental variables don't contain properties with such names",
                0, propertyLoader.getProperties().size());
    }

    @Test
    public void loadFromEnvironmentParamTypesTest() {
        //we use long and bizarre names for properties to check that we don't accidentally affect real environmental variables
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyRepositoryWithLongNames();
        environmentVariables.set("CAN_YOU_SEE_THIS_DB_USER", "Egor");
        environmentVariables.set("DELAYED_ON_SEVEN_MINUTES", "");
        environmentVariables.set("DEBUG_OR_NOT_DEBUG", "false");
        environmentVariables.set("TTL_PPL_ZZB_MGG", "5");
        environmentVariables.set("DNskfjsadkfasjdflsafj", "3.1415");
        environmentVariables.set("InsaneMadCrazyThing", "Opa");

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        propertyLoader.loadFromEnvironment();
        Assert.assertEquals("Check that we we have only 5 properties",
                5, propertyLoader.getProperties().size());
        Assert.assertTrue("Check parameterless property exists",
                propertyLoader.getProperties().containsKey("DELAYED_ON_SEVEN_MINUTES"));
        Assert.assertNull("Check parameterless property",
                propertyLoader.getProperties().get("DELAYED_ON_SEVEN_MINUTES"));
        Assert.assertFalse("Check boolean property", Boolean.parseBoolean(propertyLoader.getProperties().get("DEBUG_OR_NOT_DEBUG")));
        Assert.assertEquals("Check integer property",
                5, Integer.parseInt(propertyLoader.getProperties().get("TTL_PPL_ZZB_MGG")));
        Assert.assertEquals("Check float property",
                3.1415, Double.parseDouble(propertyLoader.getProperties().get("DNskfjsadkfasjdflsafj")), 0.1);
        Assert.assertNull("Check that we don't load a redundant property",
                propertyLoader.getProperties().get("InsaneMadCrazyThing"));
    }

    @Test
    public void LoaderToStringTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionaryWithSensitiveData();
        environmentVariables.set("DB_PATH", "/opt/server/oracle/12.2");
        environmentVariables.set("DB_USER", "Egor");
        environmentVariables.set("DB_PASS", "Zoldberg");
        environmentVariables.set("main_username", "Egor Konstantinovich");
        environmentVariables.set("user_password", "228282666");
        environmentVariables.set("TTL", "5");
        environmentVariables.set("DN", "3.1415");

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        propertyLoader.setThrowExceptionIfUnknownEnvPropertyFound(false);
        propertyLoader.loadFromEnvironment();

        String data = propertyLoader.toString();
        Assert.assertTrue(data.contains("DB_PATH: /opt/server/oracle/12.2"));
        Assert.assertTrue(data.contains("DB_USER: ***"));
        Assert.assertFalse(data.contains("DB_PASS"));
        Assert.assertTrue(data.contains("main_username: Egor Konstantinovich"));
        Assert.assertTrue(data.contains("user_password: ***"));
    }

    @Test
    public void loadFromEnvironmentWithPrefixParamTypesTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DELAYED", "");
        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5");
        environmentVariables.set("test_for_prefix.DN", "3.1415");
        environmentVariables.set("test_for_prefix.REDUNDANT", "BUM");
//        environmentVariables.set("test_for_prefix.INSANE", "Opa"); //we don't set this property to check that we don't load it

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        propertyLoader.setThrowExceptionIfUnknownEnvPropertyFound(false);
        propertyLoader.loadFromEnvironment("test_for_prefix.");
        Assert.assertEquals("Check that we we have only 5 properties",
                5, propertyLoader.getProperties().size());
        Assert.assertEquals("Check string property",
                "Egor", propertyLoader.getProperties().get("DB_USER"));
        Assert.assertTrue("Check parameterless property exists",
                propertyLoader.getProperties().containsKey("DELAYED"));
        Assert.assertNull("Check parameterless property",
                propertyLoader.getProperties().get("DELAYED"));
        Assert.assertFalse("Check boolean property", Boolean.parseBoolean(propertyLoader.getProperties().get("DEBUG")));
        Assert.assertEquals("Check integer property",
                5, Integer.parseInt(propertyLoader.getProperties().get("TTL")));
        Assert.assertEquals("Check float property",
                3.1415, Double.parseDouble(propertyLoader.getProperties().get("DN")), 0.1);
        Assert.assertNull("Check that we don't load a redundant property",
                propertyLoader.getProperties().get("REDUNDANT"));
        Assert.assertNull("Check that we don't load a field that is not in property registry",
                propertyLoader.getProperties().get("INSANE"));
    }

    @Test
    public void loadFromEnvironmentWithPrefixRedundantPropertyTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DELAYED", "");
        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5");
        environmentVariables.set("test_for_prefix.DN", "3.1415");
        environmentVariables.set("test_for_prefix.REDUNDANT", "BUM");
//        environmentVariables.set("test_for_prefix.INSANE", "Opa");

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
//        propertyLoader.setThrowExceptionIfUnknownEnvPropertyFound(true) - by default
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("Check that if environment prefix is set and setThrowExceptionIfUnknownEnvPropertyFound is true (by default) +" +
                    "redundant property cause exception");
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if environment prefix is set and setThrowExceptionIfUnknownEnvPropertyFound is true (by default) +" +
                            "redundant property cause exception with words \"unknown property\" and property name and prefix",
                    e.getMessage().contains("Unknown property \"REDUNDANT\"") && e.getMessage().contains("prefix \"test_for_prefix.\""));
        }
    }

    @Test
    public void loadFromPropertyErroneousIntCheckTypesTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();

        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5g");

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (NumberFormatException e) {
            Assert.assertTrue(e.getMessage().endsWith("integer, but input string: \"5g\""));
        }
    }


    @Test
    public void loadFromPropertyErroneousBooleanCheckTypesTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();

        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DELAYED", "");
        environmentVariables.set("test_for_prefix.DEBUG", "5");

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (Exception e) {
            Assert.assertEquals("Unknown boolean value 5 for property DEBUG", e.getMessage());
        }
    }

    @Test
    public void loadFromPropertyErroneousDoubleCheckTypesTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();

        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5,55");

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (NumberFormatException e) {
            Assert.assertTrue(e.getMessage().endsWith("integer, but input string: \"5,55\""));
        }
    }

    @Test
    public void loadFromPropertyErroneousParameterlessCheckTypesTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();

        environmentVariables.set("test_for_prefix.DELAYED", "Egor");

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Property \"DELAYED\" is parameterless, but its value is Egor", e.getMessage());
        }
    }

    @Test
    public void loadFromPropertiesFromResourceFileTest() throws URISyntaxException {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        File file = new File(getClass().getClassLoader().getResource("properties.properties").toURI());
        propertyLoader.loadFromFile(file);

        Assert.assertEquals(5, propertyLoader.getProperties().size());
        Assert.assertEquals("Nongor", propertyLoader.getProperties().get("DB_USER"));
        Assert.assertTrue(propertyLoader.getProperties().containsKey("DELAYED"));
        Assert.assertNull(propertyLoader.getProperties().get("DELAYED"));
        Assert.assertFalse(Boolean.parseBoolean(propertyLoader.getProperties().get("DEBUG")));
        Assert.assertEquals(5, Integer.parseInt(propertyLoader.getProperties().get("TTL")));
        Assert.assertEquals(3.1415, Double.parseDouble(propertyLoader.getProperties().get("DN")), 0.1);
        Assert.assertNull(propertyLoader.getProperties().get("REDUNDANT"));
        Assert.assertNull(propertyLoader.getProperties().get("INSANE"));
    }

    @Test
    public void loadFromOuterFileTest() throws IOException {
        File temp = SharedTestCommands.generateTempPropertyFile();
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        propertyLoader.loadFromFile(temp);

        Assert.assertEquals(5, propertyLoader.getProperties().size());
    }

    @Test
    public void setDefaultsTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        Assert.assertEquals("Check properties count after creation", 0, propertyLoader.getProperties().size());

        String[] cmdArgs = new String[]{"--DEBUG", "false", "--DB_USER", "User", "--DB_PATH", "C:\\test.txt", "--DB_PASSWORD", "123456"};
        propertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check properties count after loading", 4, propertyLoader.getProperties().size());
        Assert.assertNull("Make sure that there's no property with name DelayTime", propertyLoader.getProperties().get("DelayTime"));
        Assert.assertNull("Make sure that there's no property with name TTL", propertyLoader.getProperties().get("TTL"));

        propertyLoader.setDefaultIfIsNotSet();
        Assert.assertEquals("Check properties count after settings defaults", 7, propertyLoader.getProperties().size());
        Assert.assertEquals("me", propertyLoader.getProperties().get("main_username"));
        Assert.assertEquals("Check DelayTime value", "60s", propertyLoader.getProperties().get("DelayTime"));
        Assert.assertEquals("Check TTL value", 3000, Integer.parseInt(propertyLoader.getProperties().get("TTL")));
    }

    @Test
    public void comprehensiveLoadTest() {
        File temp = SharedTestCommands.generateTempPropertyFile();
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        String[] cmdArgs = new String[]{"--DEBUG", "false", "--DB_USER", "User", "--DB_path", "/opt/server/db", "--SERVER_URL", "xerby.ru"};
        environmentVariables.set("test_for_prefix.DEBUG", "true");
        environmentVariables.set("test_for_prefix.DB_USER", "Admin");
        environmentVariables.set("test_for_prefix.SERVER_URL", "Localhost");
        environmentVariables.set("test_for_prefix.scheduled", "");
        environmentVariables.set("test_for_prefix.CITY", "London");

        propertyLoader.buildProperties(cmdArgs, temp.getPath(), "test_for_prefix.", "properties.properties");

        Assert.assertEquals("Check properties count after loading", 11, propertyLoader.getProperties().size());

        //properties from cmd
        Assert.assertFalse(Boolean.parseBoolean(propertyLoader.getProperties().get("DEBUG")));
        Assert.assertEquals("User", propertyLoader.getProperties().get("db_user"));
        Assert.assertEquals("xerby.ru", propertyLoader.getProperties().get("SERVER_URL"));
        Assert.assertEquals("/opt/server/db", propertyLoader.getProperties().get("Db_PATH"));

        //properties from outer properties file
        Assert.assertEquals("Aeons", propertyLoader.getProperties().get("DelayTime"));
        Assert.assertEquals(1000, Integer.parseInt(propertyLoader.getProperties().get("Ttl")));
        Assert.assertEquals(2.86, Double.parseDouble(propertyLoader.getProperties().get("DN")), 0.1);


        //properties from environment
        Assert.assertTrue(propertyLoader.getProperties().containsKey("Scheduled"));
        Assert.assertEquals("London", propertyLoader.getProperties().get("CITY"));

        //properties from inner properties file
        Assert.assertTrue(propertyLoader.getProperties().containsKey("DELAYED"));

        //properties from default
        Assert.assertEquals("me", propertyLoader.getProperties().get("main_username"));
    }

    @Test
    public void notAllRequiredPropertiesWereDefinedTest() {
        String temp = SharedTestCommands.generateTempPropertyFile().getPath();
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        String[] cmdArgs = new String[]{"--DEBUG", "false", "--DB_USER", "User", "--SERVER_URL", "xerby.ru"};
        environmentVariables.set("test_for_prefix.DEBUG", "true");
        environmentVariables.set("test_for_prefix.DB_USER", "Admin");
        environmentVariables.set("test_for_prefix.SERVER_URL", "Localhost");
        environmentVariables.set("test_for_prefix.SCHEDULED", "");
        environmentVariables.set("test_for_prefix.CITY", "London");

        try {
            propertyLoader.buildProperties(cmdArgs, temp, "test_for_prefix.", null);
            Assert.fail("Should throw exception, but it didn't");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("property \"db_path\" is required, but it's not set", e.getMessage().toLowerCase());
        }
    }

    @Test
    public void caseSensitiveLoadTest() {
        String temp = SharedTestCommands.generateTempPropertyFile().getPath();
        PropertyDictionary propertyDictionary = SharedTestCommands.createCaseSensetiveTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        String[] cmdArgs = new String[]{"--DB_USER", "User", "--DB_path", "/opt/server/db"};
        environmentVariables.set("test_for_prefix.DB_Path", "Admin");
        environmentVariables.set("test_for_prefix.SERVER_URL", "Localhost");
        environmentVariables.set("test_for_prefix.scheduled", "");

        try {
            propertyLoader.buildProperties(cmdArgs, temp, "test_for_prefix.", null);
            Assert.fail("Should throw exception, but it didn't");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("unknown property \"db_user\" was found in command line arguments", e.getMessage().toLowerCase());
        }

        cmdArgs = new String[]{"--DB_user", "User", "--DB_PATH", "/opt/server/db"};
        try {
            propertyLoader.buildProperties(cmdArgs, temp, "test_for_prefix.", null);
            Assert.fail("Should throw exception, but it didn't");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("unknown property \"db_path\" was found in environment (prefix \"test_for_prefix.\")", e.getMessage().toLowerCase());
        }

        environmentVariables.clear("test_for_prefix.DB_Path", "test_for_prefix.SERVER_URL", "test_for_prefix.scheduled");
        environmentVariables.set("test_for_prefix.ttl", "31337");

        propertyLoader.buildProperties(cmdArgs, temp, "test_for_prefix.", null);
        Assert.assertEquals(3, propertyLoader.getProperties().size());

        Assert.assertNull(propertyLoader.getProperties().get("Ttl"));
        Assert.assertEquals(31337, Integer.parseInt(propertyLoader.getProperties().get("ttl")));
    }

    @Test
    public void redefineExternalPropertyFileFromCmdTest() {
        String temp = SharedTestCommands.generateTempPropertyFile().getPath();
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        String propertyPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "other_properties.properties";

        String[] cmdArgs = new String[]{"--DB_USER", "User", "--property-FILE", propertyPath, "--DB_path", "/opt/server/db"};

        environmentVariables.set("test.MAIN_USERNAME", "Luigi");
        environmentVariables.set("test.CITY", "London");
        environmentVariables.set("test.DB_path", "Localhost");

        propertyLoader.buildProperties(cmdArgs, temp, "test.", "properties.properties");
        var properties = propertyLoader.getProperties();

        Assert.assertEquals("Check properties count after loading", 10, properties.size());

        //properties from cmd
        Assert.assertFalse("property path to property-file must be missing", properties.containsKey("property-FILE"));
        Assert.assertEquals("User", properties.get("db_user"));
        Assert.assertEquals("/opt/server/db", properties.get("Db_PATH"));

        //redefined property-file
        Assert.assertEquals("Mario", properties.get("main_username"));
        Assert.assertEquals("2 days", properties.get("DelayTime"));
        Assert.assertFalse(properties.containsKey("db_pass"));
        Assert.assertEquals("123456", properties.get("db_password"));

        //Env
        Assert.assertEquals("London", properties.get("CITY"));

        //properties from outer properties file
        Assert.assertEquals(5, Integer.parseInt(properties.get("Ttl")));
        Assert.assertEquals(3.1415, Double.parseDouble(properties.get("DN")), 0.1);
    }

    @Test
    public void redefineExternalPropertyFileFromEnvTest() {
        String temp = SharedTestCommands.generateTempPropertyFile().getPath();
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        String propertyPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "other_properties.properties";

        String[] cmdArgs = new String[]{"--DB_USER", "User", "--DB_path", "/opt/server/db"};

        environmentVariables.set("test.MAIN_USERNAME", "Luigi");
        environmentVariables.set("test.CITY", "London");
        environmentVariables.set("test.property-FILE", propertyPath);
        environmentVariables.set("test.DB_path", "Localhost");

        propertyLoader.buildProperties(cmdArgs, temp, "test.", "properties.properties");
        var properties = propertyLoader.getProperties();

        Assert.assertEquals("Check properties count after loading", 10, properties.size());

        //properties from cmd
        Assert.assertFalse("property path to property-file must be missing", properties.containsKey("property-FILE"));
        Assert.assertEquals("User", properties.get("db_user"));
        Assert.assertEquals("/opt/server/db", properties.get("Db_PATH"));

        //redefined property-file
        Assert.assertEquals("Mario", properties.get("main_username"));
        Assert.assertEquals("2 days", properties.get("DelayTime"));
        Assert.assertFalse(properties.containsKey("db_pass"));
        Assert.assertEquals("123456", properties.get("db_password"));

        //Env
        Assert.assertEquals("London", properties.get("CITY"));

        //properties from outer properties file
        Assert.assertEquals(5, Integer.parseInt(properties.get("Ttl")));
        Assert.assertEquals(3.1415, Double.parseDouble(properties.get("DN")), 0.1);
    }

    @Test
    public void cantRedefineExternalPropertyFileFromEnvTest() {
        String temp = SharedTestCommands.generateTempPropertyFile().getPath();
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        propertyLoader.setCanRedefineExternalPropertyFile(false);

        String propertyPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "other_properties.properties";

        String[] cmdArgs = new String[]{"--DB_USER", "User", "--DB_path", "/opt/server/db"};

        environmentVariables.set("test.MAIN_USERNAME", "Luigi");
        environmentVariables.set("test.CITY", "London");
        environmentVariables.set("test.property-FILE", propertyPath);
        environmentVariables.set("test.DB_path", "Localhost");

        try {
            propertyLoader.buildProperties(cmdArgs, temp, "test.", "properties.properties");
            Assert.fail("Should throw exception, but it didn't");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Unknown property \"property-FILE\" was found in environment (prefix \"test.\")", e.getMessage());
        }
    }

    @Test
    public void parametersConsistencyTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        environmentVariables.set("test.MAIN_USERNAME", "Luigi");
        environmentVariables.set("test.Delayed", "certainly");
        environmentVariables.set("test.DB_path", "Localhost");

        try {
            propertyLoader.buildProperties(null, null, "test.", "properties.properties");
            Assert.fail("Should throw exception, but it didn't");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Property \"Delayed\" is parameterless, but its value is certainly", e.getMessage());
        }

        environmentVariables.set("test.MAIN_USERNAME", "Wario");
        environmentVariables.set("test.Delayed", null);
        environmentVariables.set("test.DB_path", null);

        try {
            propertyLoader.buildProperties(null, null, "test.", "properties.properties");
            Assert.fail("Should throw exception, but it didn't");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Property \"DB_PATH\" is required, but it's not set", e.getMessage());
        }
    }
}
