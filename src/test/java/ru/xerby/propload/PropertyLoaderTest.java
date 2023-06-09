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
    public void loadFromCmdArgsWindowsCompatibilityOptionTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "/SCHEDULED", "/DB_USER=user", "--DB_PASSWORD", "password"};

        PropertyLoader windowsPropertyLoader = new PropertyLoader(propertyRepository);
        windowsPropertyLoader.setEnabledWindowsKeyCompatibility(true);
        windowsPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 4, windowsPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value of linux-style property", "5min", windowsPropertyLoader.getProperties().get("DelayTime"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                windowsPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertEquals("Check parameter value of windows-style property", "user", windowsPropertyLoader.getProperties().get("DB_USER"));

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        //propertyLoader.setEnabledWindowsKeyCompatibility(false) - by default
        try {
            propertyLoader.loadFromCmdArgs(cmdArgs);
            Assert.fail("Check that if windows compatibility is disabled, windows-style keys cause exception");
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if windows compatibility disabled, windows-style keys cause an exception with words \"unbound token\" and token name"
                    , e.getMessage().toLowerCase().contains("unbound token") && e.getMessage().contains("SCHEDULED"));
        }
    }


    @Test
    public void loadFromCmdArgsUnboundTokenFoundOptionTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "--SCHEDULED", "--DB_USER=user", "DB_PASSWORD", "password"};

        PropertyLoader forgivingPropertyLoader = new PropertyLoader(propertyRepository);
        forgivingPropertyLoader.setThrowExceptionIfUnboundTokenFound(false);
        forgivingPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all proper properties", 3, forgivingPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value", "user", forgivingPropertyLoader.getProperties().get("DB_USER"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                forgivingPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertFalse("Check that redundant token absent in properties keys or values",
                forgivingPropertyLoader.getProperties().containsKey("DB_PASSWORD") || forgivingPropertyLoader.getProperties().containsValue("DB_PASSWORD")
                        || forgivingPropertyLoader.getProperties().containsKey("password") || forgivingPropertyLoader.getProperties().containsValue("password"));

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        //propertyLoader.setThrowExceptionIfUnboundTokenFound(true) - by default
        try {
            propertyLoader.loadFromCmdArgs(cmdArgs);
            Assert.fail("Check that if ignoring redundant token is disabled, redundant token cause exception");
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if ignoring redundant token is disabled, redundant token cause an exception with words \"unbound token\" and token name"
                    , e.getMessage().toLowerCase().contains("unbound token") && e.getMessage().contains("DB_PASSWORD"));
        }
    }

    @Test
    public void loadFromCmdArgsParametrizedWithoutEqualSignOptionTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "--SCHEDULED", "--DB_USER=user"};

        PropertyLoader forgivingPropertyLoader = new PropertyLoader(propertyRepository);
        //forgivingPropertyLoader.setParametrizedWithoutEqualSignAllowed(true) - by default
        forgivingPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 3, forgivingPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value", "5min", forgivingPropertyLoader.getProperties().get("DelayTime"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                forgivingPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertEquals("Check surely parametrized parameter value", "user", forgivingPropertyLoader.getProperties().get("DB_USER"));

        PropertyLoader strictPropertyLoader = new PropertyLoader(propertyRepository);
        strictPropertyLoader.setParametrizedWithoutEqualSignAllowed(false);
        try {
            strictPropertyLoader.loadFromCmdArgs(cmdArgs);
            Assert.fail("Check that if parameters can be specify only with an equal sign, attempt to specify it without the sign must cause an exception");
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if parameters can be specify only with an equal sign, attempt to specify it without the sign it will cause" +
                            "an exception with words \"parametrized without equal\" and property name"
                    , e.getMessage().toLowerCase().contains("parametrized without equal") && e.getMessage().contains("DelayTime"));
        }

        PropertyLoader otherPropertyLoader = new PropertyLoader(propertyRepository);
        otherPropertyLoader.setParametrizedWithoutEqualSignAllowed(false);
        otherPropertyLoader.loadFromCmdArgs(new String[]{"--DelayTime", "--SCHEDULED", "--DB_PASSWORD=password"});
        Assert.assertEquals("Check that we loaded all the properties", 3, otherPropertyLoader.getProperties().size());
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                otherPropertyLoader.getProperties().get("DelayTime"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                otherPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertEquals("Check parameter value", "password", otherPropertyLoader.getProperties().get("DB_PASSWORD"));
    }


    @Test
    public void loadFromCmdArgsUnknownCmdPropertyFoundOptionTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "--SCHEDULED", "--DB_UNER=user", "--lala", "--DB_PASSWORD", "password"};

        PropertyLoader typoForgivingPropertyLoader = new PropertyLoader(propertyRepository);
        typoForgivingPropertyLoader.setThrowExceptionIfUnknownCmdPropertyFound(false);
        typoForgivingPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties that was in repository", 3, typoForgivingPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value", "5min", typoForgivingPropertyLoader.getProperties().get("DelayTime"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                typoForgivingPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertNull("Check parameter value of property which value didn't load because of typo in cmd args is null",
                typoForgivingPropertyLoader.getProperties().get("DB_USER"));
        Assert.assertNull("Check parameter value of property that wasn't in property repository but was in cmd args is null",
                typoForgivingPropertyLoader.getProperties().get("DB_UNER"));

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        //propertyLoader.setThrowExceptionIfUnknownCmdPropertyFound(true) - by default
        Assert.assertThrows("Check that if \"throw exception if unknown cmd property found\" enabled, unknown property cause an exception"
                , RuntimeException.class, () -> propertyLoader.loadFromCmdArgs(cmdArgs));
        try {
            propertyLoader.loadFromCmdArgs(cmdArgs);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if ignoring redundant properties disabled, redundant property cause exception with words \"unknown property\" and property name"
                    , e.getMessage().toLowerCase().contains("unknown property") && e.getMessage().contains("DB_UNER"));
        }
    }

    @Test
    public void loadFromCmdArgsCheckTypesTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();

        PropertyLoader goodPropertyLoader = new PropertyLoader(propertyRepository);
        goodPropertyLoader.loadFromCmdArgs(new String[]{"--TTL", "  5", "--SCHEDULED ", "--DEBUG", "faLse", "--DB_PASSWORD", "password", "--DN", "4.087     "});
        Assert.assertEquals("Check that we loaded all the properties", 5, goodPropertyLoader.getProperties().size());

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        Assert.assertThrows(NumberFormatException.class, () -> propertyLoader.loadFromCmdArgs(
                new String[]{"--TTL", "5g", "--SCHEDULED", "--DEBUG", "false", "--DB_PASSWORD", "password", "--DN", "4.087"}));
        Assert.assertThrows(IllegalArgumentException.class, () -> propertyLoader.loadFromCmdArgs(
                new String[]{"--TTL", "5", "--SCHEDULED", "--DEBUG", "farse", "--DB_PASSWORD", "password", "--DN", "4.087"}));
        Assert.assertThrows(NumberFormatException.class, () -> propertyLoader.loadFromCmdArgs(
                new String[]{"--TTL", "5", "--SCHEDULED", "--DEBUG", "false", "--DB_PASSWORD", "password", "--DN", "4,087"}));
        Assert.assertThrows(IllegalArgumentException.class, () -> propertyLoader.loadFromCmdArgs(
                new String[]{"--DELAYED", "5min", "--SCHEDULED", "--DEBUG", "false", "--DB_PASSWORD", "password", "--DN", "4,087"}));
    }

    @Test
    public void loadFromEnvironmentEmptyTest() {
        //we use long and bizarre names for properties to check that we don't accidentally affect real environmental variables
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepositoryWithLongNames();

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        propertyLoader.loadFromEnvironment();
        Assert.assertEquals("Check that we don't load any property, because environmental variables don't contain properties with such names",
                0, propertyLoader.getProperties().size());
    }

    @Test
    public void loadFromEnvironmentParamTypesTest() {
        //we use long and bizarre names for properties to check that we don't accidentally affect real environmental variables
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepositoryWithLongNames();
        environmentVariables.set("CAN_YOU_SEE_THIS_DB_USER", "Egor");
        environmentVariables.set("DELAYED_ON_SEVEN_MINUTES", "");
        environmentVariables.set("DEBUG_OR_NOT_DEBUG", "false");
        environmentVariables.set("TTL_PPL_ZZB_MGG", "5");
        environmentVariables.set("DNskfjsadkfasjdflsafj", "3.1415");
        environmentVariables.set("InsaneMadCrazyThing", "Opa");

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
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
    public void loadFromEnvironmentWithPrefixParamTypesTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DELAYED", "");
        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5");
        environmentVariables.set("test_for_prefix.DN", "3.1415");
        environmentVariables.set("test_for_prefix.REDUNDANT", "BUM");
//        environmentVariables.set("test_for_prefix.INSANE", "Opa"); //we don't set this property to check that we don't load it

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
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
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DELAYED", "");
        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5");
        environmentVariables.set("test_for_prefix.DN", "3.1415");
        environmentVariables.set("test_for_prefix.REDUNDANT", "BUM");
//        environmentVariables.set("test_for_prefix.INSANE", "Opa");

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
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
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();

        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5g");

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (NumberFormatException e) {
            Assert.assertEquals("For input string: \"5g\"", e.getMessage());
        }
    }


    @Test
    public void loadFromPropertyErroneousBooleanCheckTypesTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();

        environmentVariables.set("test_for_prefix.DB_USER", "Egor");
        environmentVariables.set("test_for_prefix.DELAYED", "");
        environmentVariables.set("test_for_prefix.DEBUG", "5");

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (Exception e) {
            Assert.assertEquals("Unknown boolean value 5 for property DEBUG", e.getMessage());
        }
    }

    @Test
    public void loadFromPropertyErroneousDoubleCheckTypesTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();

        environmentVariables.set("test_for_prefix.DEBUG", "false");
        environmentVariables.set("test_for_prefix.TTL", "5,55");

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (NumberFormatException e) {
            Assert.assertEquals("For input string: \"5,55\"", e.getMessage());
        }
    }

    @Test
    public void loadFromPropertyErroneousParameterlessCheckTypesTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();

        environmentVariables.set("test_for_prefix.DELAYED", "Egor");

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);
        try {
            propertyLoader.loadFromEnvironment("test_for_prefix.");
            Assert.fail("loadFromEnvironment must cause an exception if erroneous types are found");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Property DELAYED is not parametrized, but it's value is Egor", e.getMessage());
        }
    }

    @Test
    public void loadFromPropertiesFromResourceFileTest() throws URISyntaxException {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);

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
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);

        propertyLoader.loadFromFile(temp);

        Assert.assertEquals(5, propertyLoader.getProperties().size());
    }

    @Test
    public void setDefaultsTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);

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
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);

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
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);

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
            Assert.assertEquals("property db_path is required, but it's not set", e.getMessage().toLowerCase());
        }
    }

    @Test
    public void caseSensitiveLoadTest() {
        String temp = SharedTestCommands.generateTempPropertyFile().getPath();
        PropertyRepository propertyRepository = SharedTestCommands.createCaseSensetiveTestPropertyRepository();
        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);

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
}
