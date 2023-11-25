package ru.xerby.propload;


import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CmdParserTest {

    @Test
    public void testEmpty() {
        var props = new ParsedCmdProperties();
        Assert.assertEquals("If args is null method must return empty ParsedCmdProperties",
                props,
                ParsedCmdProperties.parse(null, true, false));

        props = new ParsedCmdProperties();
        Assert.assertEquals("If args is empty array method must return empty ParsedCmdProperties",
                props,
                ParsedCmdProperties.parse(new String[]{}, true, false));
    }

    @Test
    public void testSimpleProperty() {
        ParsedCmdProperties props;

        props = new ParsedCmdProperties();
        props.add("delayed", null, false);
        Assert.assertEquals("One simple key",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed"}, true, false));

        props = new ParsedCmdProperties();
        props.add("delayed", null, false);
        Assert.assertEquals("Windows style simple key with windows parse mode",
                props,
                ParsedCmdProperties.parse(new String[]{"/delayed"}, true, false));

        props = new ParsedCmdProperties();
        Assert.assertEquals("Windows style simple key with non-windows parse mode",
                props,
                ParsedCmdProperties.parse(new String[]{"/delayed"}, false, false));

        Assert.assertEquals("Can't recognize key with one minus parameter",
                "-d",
                ParsedCmdProperties.parse(new String[]{"-delayed"}, true, false).toString());
    }

    @Test
    public void testParametrizedProperty() {
        ParsedCmdProperties props;

        props = new ParsedCmdProperties();
        props.add("delayed", "5min", false);
        Assert.assertEquals("Check that not surely parametrized property is treated as not surely parametrized",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed", "5min"}, true, false));

        props = new ParsedCmdProperties();
        props.add("delayed", "5min", true);
        Assert.assertNotEquals("Check that not surely parametrized property is not equal surely parametrized property with the same key and value",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed", "5min"}, true, false));

        props = new ParsedCmdProperties();
        props.add("delayed", "5min", true);
        Assert.assertEquals("Check that surely parametrized property is treated as surely parametrized",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed=5min"}, true, false));

        props = new ParsedCmdProperties();
        props.add("delayed", "5min", false);
        Assert.assertNotEquals("Check that surely parametrized property is not equal not surely parametrized property with the same key and value",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed=5min"}, true, false));
    }


    @Test
    public void testFewProperties() {
        ParsedCmdProperties props;

        props = new ParsedCmdProperties();
        props.add("delayed");
        props.add("mail");
        Assert.assertEquals("Check a parameterless property followed by other property",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed", "--mail"}, true, false));

        props = new ParsedCmdProperties();
        props.add("delayed", "5min", false);
        props.add("mail");
        Assert.assertEquals("Check a parametrized property followed by other property",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed", "5min", "--mail"}, true, false));

        props = new ParsedCmdProperties();
        props.add("delayed", "5min", true);
        props.add("mail");
        Assert.assertEquals("Check a surely parametrized property parameter followed by other property",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed=5min", "--mail"}, true, false));
    }

    @Test
    public void testWindowsKeys() {
        ParsedCmdProperties props;

        props = new ParsedCmdProperties();
        props.add("delayed", "/5min", false);
        Assert.assertEquals("Check that in non-windows mode, token that started with \"/\" treated as value of previous parameter",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed", "/5min"}, false, false));

        props = new ParsedCmdProperties();
        props.add("delayed");
        props.add("5min");
        Assert.assertEquals("Check that in windows mode, token that started with \"/\" treated as parameter key",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed", "/5min"}, true, false));
    }

    @Test
    public void testRedundantToken() {
        ParsedCmdProperties props;

        props = new ParsedCmdProperties();
        props.add("delayed", "/5min", false);
        //todo:need to check logs
        Assert.assertEquals("If throwExceptionIfUnboundTokenFound is false, then redundant token just omit and is mentioned in log",
                props,
                ParsedCmdProperties.parse(new String[]{"--delayed", "/5min", "/mail"}, false, false));

        props = new ParsedCmdProperties();
        props.add("delayed", "/5min", false);
        Assert.assertThrows("If throwExceptionIfUnboundTokenFound is true, then if is there  redundant token then exception is thrown",
                RuntimeException.class,
                () -> ParsedCmdProperties.parse(new String[]{"--delayed", "/5min", "/mail"}, false, true));

        System.out.println(ParsedCmdProperties.parse(new String[]{"/delayed", "--no-ops", "pzdariki", "gigi", "/commando", "opa", "--size=plpl"}, true, false));
    }

    @Test
    public void testEmptyAndClear() {
        var props = new ParsedCmdProperties();

        Assert.assertTrue("Property is empty after creation", props.isEmpty());

        props.add("delayed", "5min", false);
        props.add("mail");
        props.add("no-ops", "false", true);

        Assert.assertFalse("Property is not empty after adding", props.isEmpty());
        props.clear();
        Assert.assertTrue("Property is empty after clearing", props.isEmpty());
    }

    @Test
    public void testGetMethods() {
        var props = new ParsedCmdProperties();

        props.add("delayed", "5min", false);
        props.add("mail");
        props.add("no-ops", "false", true);

        Assert.assertTrue("Can find all 3 keys", props.containsKey("delayed") && props.containsKey("mail") && props.containsKey("no-ops"));
        Assert.assertFalse("Can't find all non-existent key", props.containsKey("govern"));

        Assert.assertEquals("Check value for the first property", "5min", props.getValue("delayed"));
        Assert.assertNull("Value of mail is null", props.getValue("mail"));
        Assert.assertEquals("Check value for the second property", "false", props.getValue("no-ops"));
        Assert.assertNull("Value of non-existent is null", props.getValue("govern"));

        Assert.assertFalse("Check if the first property surely", props.isSurelyParametrized("delayed"));
        Assert.assertFalse("Check if the second property surely", props.isSurelyParametrized("mail"));
        Assert.assertTrue("Check if the third property surely", props.isSurelyParametrized("no-ops"));
        Assert.assertFalse("Check non-existent property surely", props.isSurelyParametrized("govern"));
    }


    @Test
    public void testMalformedAddProperty() {
        var props = new ParsedCmdProperties();

        Assert.assertThrows("Can't add property without name",
                RuntimeException.class,
                () -> props.add(null, "5min", false));

        Assert.assertThrows("Can't add property without parameter value with isSurelyParameterized=true",
                RuntimeException.class,
                () -> props.add("no-ops", null, true));
    }


    @Test
    public void testGetProperty() {
        var props = new ParsedCmdProperties();

        props.add("delayed", "5min", false);
        props.add("mail");
        props.add("no-ops", "false", true);

        Assert.assertEquals("Can find all 3 keys",
                new ParsedCmdProperty("delayed", "5min", false),
                props.getParsedCmdProperty("delayed"));
        Assert.assertNotEquals("Can find all 3 keys",
                new ParsedCmdProperty("delayed", "2min", false),
                props.getParsedCmdProperty("delayed"));
        Assert.assertNotEquals("Can find all 3 keys",
                new ParsedCmdProperty("delayed", "5min", true),
                props.getParsedCmdProperty("delayed"));
        Assert.assertNotEquals("Can find all 3 keys",
                new ParsedCmdProperty("delay", "5min", true),
                props.getParsedCmdProperty("delayed"));
        Assert.assertNull("Can't find all non-existent key", props.getParsedCmdProperty("dead"));
    }

    @Test
    public void testToStringAndIterator() {
        var props = new ParsedCmdProperties();

        props.add("delayed", "5min", false);
        props.add("mail");
        props.add("no-ops", "false", true);

        List<String> strings = new ArrayList<>();

        strings.add("--delayed 5min");
        strings.add("--mail");
        strings.add("--no-ops=false");

        for (ParsedCmdProperty prop : props) {
            Assert.assertTrue("Check toString() method " + prop.getKey(), strings.contains(prop.toString()));
            strings.remove(prop.toString());
        }
    }
}
