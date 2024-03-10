package ru.xerby.propload;

import org.junit.Assert;
import org.junit.Test;

public class PropertyDefinitionTest {


    @Test
    public void consistencyTest() {
        try {
            new PropertyDefinition("DB_PATH", "Path to database", "BOOM!",
                    PropertyDefinition.ParametrizationDegree.PARAMETER_PROHIBITED, true, PropertyDefinition.ParamType.STRING);
            Assert.fail("Property which was created by createKeyValueRequiredProperty must not have default value");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Can't set default value for parameterless property", e.getMessage());
        }

        try {
            new PropertyDefinition("DB_PATH", "Path to database", "BOOM!",
                    PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDefinition.ParamType.STRING);
            Assert.fail("Property can't be isRequired and hasDefaultValue at the same time");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Can't be isRequired and hasDefaultValue at the same time", e.getMessage());
        }
    }

    @Test
    public void cannotChangeNameTest() {
        try {
            PropertyDefinition definition = new PropertyDefinition("DB_PATH", "Path to database", "BOOM!",
                    PropertyDefinition.ParametrizationDegree.PARAMETER_OPTIONAL, false, PropertyDefinition.ParamType.STRING);
            definition.setName("DB_PAT");
            Assert.fail("It's prohibit to change name of property after it was created");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("prohibit to change name"));
        }
    }
}
