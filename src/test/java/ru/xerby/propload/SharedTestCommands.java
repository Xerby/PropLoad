package ru.xerby.propload;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Properties;

public class SharedTestCommands {

    public static PropertyDictionary createTestPropertyDictionary() {
        PropertyDictionary propertyDictionary = new PropertyDictionary();

        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_PATH", "Path to database"));
        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_user", "Database user"));
        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueOptionalProperty("DB_Password", "Database password"));
        propertyDictionary.registerProperty(new PropertyDefinition("main_username", null, "me",
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.STRING, 'u', "user", "username", "name"));
        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueOptionalProperty("server_URL", "SR2E server URL"));
        propertyDictionary.registerProperty(PropertyDefinition.createParameterlessProperty("Delayed",
                "Program will show main window not right away after run, but in a minute (suitable for use with autorun, so as not to annoy the user with the appearance of the program even before the operating system is fully loaded)"));
        propertyDictionary.registerProperty(PropertyDefinition.createParameterlessProperty("scheduled",
                "Program will show main window not right away after run, but check schedule and show only if time is right (suitable use with autorun)"));
        propertyDictionary.registerProperty(new PropertyDefinition("DelayTime", "Value for delay after program starts", "60s",
                PropertyDefinition.ParametrizationDegree.PARAMETER_OPTIONAL, false, PropertyDefinition.ParamType.STRING));
        propertyDictionary.registerProperty(new PropertyDefinition("DEBUG", "Is debug mode enabled", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDefinition.ParamType.BOOLEAN, 'd'));
        propertyDictionary.registerProperty(new PropertyDefinition("TTL", "Server timeout (in millis)", "3000",
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.INTEGER));
        propertyDictionary.registerProperty(new PropertyDefinition("DN", "I don't know what property it must be", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.FLOAT, '\0', "dunno"));
        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueOptionalProperty("CITY", null));

        return propertyDictionary;
    }


    public static PropertyDictionary createCaseSensetiveTestPropertyDictionary() {
        PropertyDictionary propertyDictionary = new PropertyDictionary(true);

        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_PATH", "Path to database"));
        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_user", "Database user"));
        propertyDictionary.registerProperty(new PropertyDefinition("main_username", null, null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.STRING, 'u', "user", "username", "name"));
        propertyDictionary.registerProperty(new PropertyDefinition("ttl", "Server timeout (in millis)", "3000",
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.INTEGER, 'T'));
        propertyDictionary.registerProperty(new PropertyDefinition("Dn", "I don't know what property it must be", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.FLOAT));

        return propertyDictionary;
    }


    public static PropertyDictionary createTestPropertyRepositoryWithLongNames() {
        PropertyDictionary propertyDictionary = new PropertyDictionary();

        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("I_DONT_KNOW_WHY_WE_SAY_ABOUT_DB_PATH", "Path to database"));
        propertyDictionary.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("CAN_YOU_SEE_THIS_DB_USER", "Database user"));
        propertyDictionary.registerProperty(PropertyDefinition.createParameterlessProperty("DELAYED_ON_SEVEN_MINUTES",
                "Program will show main window not right away after run, but in a minute (suitable for use with autorun, so as not to annoy the user with the appearance of the program even before the operating system is fully loaded)"));
        propertyDictionary.registerProperty(new PropertyDefinition("DEBUG_OR_NOT_DEBUG", "Is debug mode enabled", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDefinition.ParamType.BOOLEAN));
        propertyDictionary.registerProperty(new PropertyDefinition("TTL_PPL_ZZB_MGG", "Server timeout (in millis)", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.INTEGER));
        propertyDictionary.registerProperty(new PropertyDefinition("DNskfjsadkfasjdflsafj", "I don't know what property it must be", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.FLOAT));

        return propertyDictionary;
    }

    @SneakyThrows
    public static File generateTempPropertyFile() {
        Properties properties = new Properties();
        properties.setProperty("DB_USER", "Egor");
        properties.setProperty("SERVER_URL", "https://google.com");
        properties.setProperty("TTL", "1000");
        properties.setProperty("DelayTime", "Aeons");
        properties.setProperty("dn", "2.86");
        properties.setProperty("REDUNDANT", "12");

        File temp = Files.createTempFile("temp", ".properties").toFile();
        properties.store(new FileOutputStream(temp), "Test properties");
        return temp;
    }
}
