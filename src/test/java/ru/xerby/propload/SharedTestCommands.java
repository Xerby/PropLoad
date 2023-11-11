package ru.xerby.propload;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Properties;

public class SharedTestCommands {

    public static PropertyRepository createTestPropertyRepository() {
        PropertyRepository propertyRepository = new PropertyRepository();

        propertyRepository.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_PATH", "Path to database"));
        propertyRepository.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_user", "Database user"));
        propertyRepository.registerProperty(PropertyDefinition.createKeyValueOptionalProperty("DB_Password", "Database password"));
        propertyRepository.registerProperty(new PropertyDefinition("main_username", null, "me",
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.STRING));
        propertyRepository.registerProperty(PropertyDefinition.createKeyValueOptionalProperty("server_URL", "SR2E server URL"));
        propertyRepository.registerProperty(PropertyDefinition.createParameterlessProperty("Delayed",
                "Program will show main window not right away after run, but in a minute (suitable for use with autorun, so as not to annoy the user with the appearance of the program even before the operating system is fully loaded)"));
        propertyRepository.registerProperty(PropertyDefinition.createParameterlessProperty("scheduled",
                "Program will show main window not right away after run, but check schedule and show only if time is right (suitable use with autorun)"));
        propertyRepository.registerProperty(new PropertyDefinition("DelayTime", "Value for delay after program starts", "60s",
                PropertyDefinition.ParametrizationDegree.PARAMETER_OPTIONAL, false, PropertyDefinition.ParamType.STRING));
        propertyRepository.registerProperty(new PropertyDefinition("DEBUG", "Is debug mode enabled", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDefinition.ParamType.BOOLEAN));
        propertyRepository.registerProperty(new PropertyDefinition("TTL", "Server timeout (in millis)", "3000",
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.INTEGER));
        propertyRepository.registerProperty(new PropertyDefinition("DN", "I don't know what property it must be", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.FLOAT));
        propertyRepository.registerProperty(PropertyDefinition.createKeyValueOptionalProperty("CITY", null));

        return propertyRepository;
    }


    public static PropertyRepository createCaseSensetiveTestPropertyRepository() {
        PropertyRepository propertyRepository = new PropertyRepository(true);

        propertyRepository.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_PATH", "Path to database"));
        propertyRepository.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("DB_user", "Database user"));
        propertyRepository.registerProperty(new PropertyDefinition("ttl", "Server timeout (in millis)", "3000",
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.INTEGER));
        propertyRepository.registerProperty(new PropertyDefinition("Dn", "I don't know what property it must be", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.FLOAT));

        return propertyRepository;
    }


    public static PropertyRepository createTestPropertyRepositoryWithLongNames() {
        PropertyRepository propertyRepository = new PropertyRepository();

        propertyRepository.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("I_DONT_KNOW_WHY_WE_SAY_ABOUT_DB_PATH", "Path to database"));
        propertyRepository.registerProperty(PropertyDefinition.createKeyValueRequiredProperty("CAN_YOU_SEE_THIS_DB_USER", "Database user"));
        propertyRepository.registerProperty(PropertyDefinition.createParameterlessProperty("DELAYED_ON_SEVEN_MINUTES",
                "Program will show main window not right away after run, but in a minute (suitable for use with autorun, so as not to annoy the user with the appearance of the program even before the operating system is fully loaded)"));
        propertyRepository.registerProperty(new PropertyDefinition("DEBUG_OR_NOT_DEBUG", "Is debug mode enabled", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDefinition.ParamType.BOOLEAN));
        propertyRepository.registerProperty(new PropertyDefinition("TTL_PPL_ZZB_MGG", "Server timeout (in millis)", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.INTEGER));
        propertyRepository.registerProperty(new PropertyDefinition("DNskfjsadkfasjdflsafj", "I don't know what property it must be", null,
                PropertyDefinition.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDefinition.ParamType.FLOAT));

        return propertyRepository;
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
