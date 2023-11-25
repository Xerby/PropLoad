package ru.xerby.propload;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class CmdLoaderTest {

    @Test
    public void loadFromCmdArgsWindowsCompatibilityOptionTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "/SCHEDULED", "/DB_USER=user", "--DB_PASSWORD", "password"};

        PropertyLoader windowsPropertyLoader = new PropertyLoader(propertyDictionary);
        windowsPropertyLoader.setEnabledWindowsKeyCompatibility(true);
        windowsPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 4, windowsPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value of linux-style property", "5min", windowsPropertyLoader.getProperties().get("DelayTime"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                windowsPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertEquals("Check parameter value of windows-style property", "user", windowsPropertyLoader.getProperties().get("DB_USER"));

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        //propertyLoader.setEnabledWindowsKeyCompatibility(false) - by default
        try {
            propertyLoader.loadFromCmdArgs(cmdArgs);
            Assert.fail("Check that if windows compatibility is disabled, windows-style keys cause exception");
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if windows compatibility disabled, windows-style keys cause an exception with words \"dangling token\" and token name"
                    , e.getMessage().toLowerCase().contains("dangling token") && e.getMessage().contains("SCHEDULED"));
        }
    }


    @Test
    public void loadFromCmdArgsUnboundTokenFoundOptionTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "--SCHEDULED", "--DB_USER=user", "DB_PASSWORD", "password"};

        PropertyLoader forgivingPropertyLoader = new PropertyLoader(propertyDictionary);
        forgivingPropertyLoader.setThrowExceptionIfUnboundTokenFound(false);
        forgivingPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all proper properties", 3, forgivingPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value", "user", forgivingPropertyLoader.getProperties().get("DB_USER"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                forgivingPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertFalse("Check that redundant token absent in properties keys or values",
                forgivingPropertyLoader.getProperties().containsKey("DB_PASSWORD") || forgivingPropertyLoader.getProperties().containsValue("DB_PASSWORD")
                        || forgivingPropertyLoader.getProperties().containsKey("password") || forgivingPropertyLoader.getProperties().containsValue("password"));

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
        //propertyLoader.setThrowExceptionIfUnboundTokenFound(true) - by default
        try {
            propertyLoader.loadFromCmdArgs(cmdArgs);
            Assert.fail("Check that if ignoring redundant token is disabled, redundant token cause exception");
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if ignoring redundant token is disabled, redundant token cause an exception with words \"dangling token\" and token name"
                    , e.getMessage().toLowerCase().contains("dangling token") && e.getMessage().contains("DB_PASSWORD"));
        }
    }

    @Test
    public void loadFromCmdArgsParametrizedWithoutEqualSignOptionTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "--SCHEDULED", "--DB_USER=user"};

        PropertyLoader forgivingPropertyLoader = new PropertyLoader(propertyDictionary);
        //forgivingPropertyLoader.setParametrizedWithoutEqualSignAllowed(true) - by default
        forgivingPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 3, forgivingPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value", "5min", forgivingPropertyLoader.getProperties().get("DelayTime"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                forgivingPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertEquals("Check surely parametrized parameter value", "user", forgivingPropertyLoader.getProperties().get("DB_USER"));

        PropertyLoader strictPropertyLoader = new PropertyLoader(propertyDictionary);
        strictPropertyLoader.setParametrizedWithoutEqualSignAllowed(false);
        try {
            strictPropertyLoader.loadFromCmdArgs(cmdArgs);
            Assert.fail("Check that if parameters can be specify only with an equal sign, attempt to specify it without the sign must cause an exception");
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if parameters can be specify only with an equal sign, attempt to specify it without the sign it will cause" +
                            "an exception with words \"parametrized without equal\" and property name"
                    , e.getMessage().toLowerCase().contains("parametrized without equal") && e.getMessage().contains("DelayTime"));
        }

        PropertyLoader otherPropertyLoader = new PropertyLoader(propertyDictionary);
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
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        String[] cmdArgs = new String[]{"--DelayTime", "5min", "--SCHEDULED", "--DB_UNER=user", "--lala", "--DB_PASSWORD", "password"};

        PropertyLoader typoForgivingPropertyLoader = new PropertyLoader(propertyDictionary);
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

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
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
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();

        PropertyLoader goodPropertyLoader = new PropertyLoader(propertyDictionary);
        goodPropertyLoader.loadFromCmdArgs(new String[]{"--TTL", "  5", "--SCHEDULED ", "--DEBUG", "faLse", "--DB_PASSWORD", "password", "--DN", "4.087     "});
        Assert.assertEquals("Check that we loaded all the properties", 5, goodPropertyLoader.getProperties().size());

        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);
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
    public void loadFromCmdArgsUsingCmdAliases() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader;
        String[] cmdArgs;

        cmdArgs = new String[]{"--DelayTime", "5min", "--user=user", "--dunno", "6.5"};
        propertyLoader = new PropertyLoader(propertyDictionary);
        propertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 3, propertyLoader.getProperties().size());
        Assert.assertEquals("Check that we get property by its cmd-alias", 6.5, propertyLoader.getAsDouble("dn"), 0.1);
        Assert.assertEquals("5min", propertyLoader.getProperties().get("delaytime"));
        Assert.assertEquals("Check that we get property by its cmd-alias", "user", propertyLoader.getProperties().get("main_username"));

        cmdArgs = new String[]{"--DelayTime", "5min", "--name=grigory", "--dn", "7.8"};
        propertyLoader = new PropertyLoader(propertyDictionary);
        propertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 3, propertyLoader.getProperties().size());
        Assert.assertEquals("Check that we get property by its other cmd-alias", 7.8, propertyLoader.getAsDouble("dn"), 0.1);
        Assert.assertEquals("Check that we get property by its name", "grigory", propertyLoader.getProperties().get("main_username"));
    }

    @Test
    public void loadFromCmdArgsUsingCharCmdAlias() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader;
        String[] cmdArgs;

        cmdArgs = new String[]{"--DelayTime", "5min", "-u=oleg", "-d", "YES"};
        propertyLoader = new PropertyLoader(propertyDictionary);
        propertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 3, propertyLoader.getProperties().size());
        Assert.assertEquals("Check that we get property by its other cmd-alias", "oleg", propertyLoader.get("main_username"));
        Assert.assertTrue("Check that we get property by its name", propertyLoader.getAsBoolean("debug"));
    }

    @Test
    public void cantLoadFromCmdArgsUsingLongCmdAliasInsteadOfShort() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader;
        String[] cmdArgs;

        cmdArgs = new String[]{"--u=oleg", "--d", "YES"};
        propertyLoader = new PropertyLoader(propertyDictionary);
        Assert.assertThrows(IllegalArgumentException.class, () -> propertyLoader.loadFromCmdArgs(cmdArgs));
    }

    @Test
    public void cantLoadFromCmdArgsUsingShortCmdAliasInsteadOfLong() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader;
        String[] cmdArgs;

        cmdArgs = new String[]{"-DelayTime", "5min", "-debug=YES"};
        propertyLoader = new PropertyLoader(propertyDictionary);
        Assert.assertThrows(IllegalArgumentException.class, () -> propertyLoader.loadFromCmdArgs(cmdArgs));
    }

    @Test
    public void cantRedefineExternalPropertyFileFromCmdTest() {
        String temp = SharedTestCommands.generateTempPropertyFile().getPath();
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        propertyLoader.setCanRedefineExternalPropertyFile(false);

        String propertyPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "other_properties.properties";

        String[] cmdArgs = new String[]{"--DB_USER", "User", "--property-FILE", propertyPath, "--DB_path", "/opt/server/db"};


        try {
            propertyLoader.buildProperties(cmdArgs, temp, "test.", "properties.properties");
            Assert.fail("Should throw exception, but it didn't");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Unknown property \"property-FILE\" was found in command line arguments", e.getMessage());
        }
    }

    @Test
    public void cmdParametersConsistencyTest() {
        PropertyDictionary propertyDictionary = SharedTestCommands.createTestPropertyDictionary();
        PropertyLoader propertyLoader = new PropertyLoader(propertyDictionary);

        String[] cmdArgs = new String[]{"--MAIN_USERNAME", "Luigi", "--Delayed=certainly", "--DB_path", "Localhost"};

        try {
            propertyLoader.buildProperties(cmdArgs, null, null, "properties.properties");
            Assert.fail("When a parameterless property is initialized with a parameter using the equals sign, an exception must be thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Property \"Delayed\" is parameterless, but its value is \"certainly\"", e.getMessage());
        }

        cmdArgs = new String[]{"--MAIN_USERNAME", "Luigi", "--Delayed", "certainly", "--DB_path", "Localhost"};
        propertyLoader.buildProperties(cmdArgs, null, null, "properties.properties");

        var properties = propertyLoader.getProperties();
        Assert.assertNull("When a parameterless property is initialized with a parameter not using the equals sign, that parameter is ignored", properties.get("Delayed"));

        cmdArgs = new String[]{"--Delayed", "--DB_path", "--MAIN_USERNAME", "Wario"};

        try {
            propertyLoader.buildProperties(cmdArgs, null, null, "properties.properties");
            Assert.fail("When a parameterized property is initialized without a parameter, an exception must be thrown");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Property \"DB_path\" should have a value, but it doesn't", e.getMessage());
        }
    }
}