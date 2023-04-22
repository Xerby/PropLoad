package ru.xerby.propload;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyLoaderTest {

    @Before
    public void setUp() throws Exception {

    }


    @Test
    public void loadFromCmdArgsWindowsCompatibilityOptionTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        String[] cmdArgs = new String[]{"--DELAYED", "5min", "/SCHEDULED", "/DB_USER=user", "--DB_PASSWORD", "password"};

        PropertyLoader windowsPropertyLoader = new PropertyLoader(propertyRepository);
        windowsPropertyLoader.setEnabledWindowsKeyCompatibility(true);
        windowsPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 4, windowsPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value of linux-style property", "5min", windowsPropertyLoader.getProperties().get("DELAYED"));
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
        String[] cmdArgs = new String[]{"--DELAYED", "5min", "--SCHEDULED", "--DB_USER=user", "DB_PASSWORD", "password"};

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
        String[] cmdArgs = new String[]{"--DELAYED", "5min", "--SCHEDULED", "--DB_USER=user"};

        PropertyLoader forgivingPropertyLoader = new PropertyLoader(propertyRepository);
        //forgivingPropertyLoader.setParametrizedWithoutEqualSignAllowed(true) - by default
        forgivingPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties", 3, forgivingPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value", "5min", forgivingPropertyLoader.getProperties().get("DELAYED"));
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
                    , e.getMessage().toLowerCase().contains("parametrized without equal") && e.getMessage().contains("DELAYED"));
        }

        PropertyLoader otherPropertyLoader = new PropertyLoader(propertyRepository);
        otherPropertyLoader.setParametrizedWithoutEqualSignAllowed(false);
        otherPropertyLoader.loadFromCmdArgs(new String[]{"--DELAYED", "--SCHEDULED", "--DB_PASSWORD=password"});
        Assert.assertEquals("Check that we loaded all the properties", 3, otherPropertyLoader.getProperties().size());
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                otherPropertyLoader.getProperties().get("DELAYED"));
        Assert.assertNull("Check that parameterless property is loaded and it's value is null",
                otherPropertyLoader.getProperties().get("SCHEDULED"));
        Assert.assertEquals("Check parameter value", "password", otherPropertyLoader.getProperties().get("DB_PASSWORD"));
    }


    @Test
    public void loadFromCmdArgsUnknownCmdPropertyFoundOptionTest() {
        PropertyRepository propertyRepository = SharedTestCommands.createTestPropertyRepository();
        String[] cmdArgs = new String[]{"--DELAYED", "5min", "--SCHEDULED", "--DB_UNER=user", "--lala", "--DB_PASSWORD", "password"};

        PropertyLoader typoForgivingPropertyLoader = new PropertyLoader(propertyRepository);
        typoForgivingPropertyLoader.setThrowExceptionIfUnknownCmdPropertyFound(false);
        typoForgivingPropertyLoader.loadFromCmdArgs(cmdArgs);
        Assert.assertEquals("Check that we loaded all properties that was in repository", 3, typoForgivingPropertyLoader.getProperties().size());
        Assert.assertEquals("Check parameter value", "5min", typoForgivingPropertyLoader.getProperties().get("DELAYED"));
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
        } catch (RuntimeException e) {
            Assert.assertTrue("Check that if ignoring redundant properties disabled, redundant property cause exception with words \"unknown property\" and property name"
                    , e.getMessage().toLowerCase().contains("unknown property") && e.getMessage().contains("DB_UNER"));
        }
    }
}
