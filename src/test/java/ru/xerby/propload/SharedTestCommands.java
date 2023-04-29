package ru.xerby.propload;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Properties;

public class SharedTestCommands {

    public static PropertyRepository createTestPropertyRepository() {
        PropertyRepository propertyRepository = new PropertyRepository();

        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("DB_PATH", "Path to database"));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("DB_user", "Database user"));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueOptionalProperty("DB_Password", "Database password"));
        propertyRepository.registerProperty(new PropertyDescription("main_username", null, "me",
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.STRING));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueOptionalProperty("server_URL", "SR2E server URL"));
        propertyRepository.registerProperty(PropertyDescription.createParameterlessProperty("Delayed",
                "Program will show main window not right away after run, but in a minute (suitable for use with autorun, so as not to annoy the user with the appearance of the program even before the operating system is fully loaded)"));
        propertyRepository.registerProperty(PropertyDescription.createParameterlessProperty("scheduled",
                "Program will show main window not right away after run, but check schedule and show only if time is right (suitable use with autorun)"));
        propertyRepository.registerProperty(new PropertyDescription("DelayTime", "Value for delay after program starts", "60s",
                PropertyDescription.ParametrizationDegree.PARAMETER_OPTIONAL, false, PropertyDescription.ParamType.STRING));
        propertyRepository.registerProperty(new PropertyDescription("DEBUG", "Is debug mode enabled", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDescription.ParamType.BOOLEAN));
        propertyRepository.registerProperty(new PropertyDescription("TTL", "Server timeout (in millis)", "3000",
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.INTEGER));
        propertyRepository.registerProperty(new PropertyDescription("DN", "I don't know what property it must be", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.FLOAT));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueOptionalProperty("CITY", null));

        return propertyRepository;
    }


    public static PropertyRepository createCaseSensetiveTestPropertyRepository() {
        PropertyRepository propertyRepository = new PropertyRepository(true);

        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("DB_PATH", "Path to database"));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("DB_user", "Database user"));
        propertyRepository.registerProperty(new PropertyDescription("ttl", "Server timeout (in millis)", "3000",
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.INTEGER));
        propertyRepository.registerProperty(new PropertyDescription("Dn", "I don't know what property it must be", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.FLOAT));

        return propertyRepository;
    }


    public static PropertyRepository createTestPropertyRepositoryWithLongNames() {
        PropertyRepository propertyRepository = new PropertyRepository();

        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("I_DONT_KNOW_WHY_WE_SAY_ABOUT_DB_PATH", "Path to database"));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("CAN_YOU_SEE_THIS_DB_USER", "Database user"));
        propertyRepository.registerProperty(PropertyDescription.createParameterlessProperty("DELAYED_ON_SEVEN_MINUTES",
                "Program will show main window not right away after run, but in a minute (suitable for use with autorun, so as not to annoy the user with the appearance of the program even before the operating system is fully loaded)"));
        propertyRepository.registerProperty(new PropertyDescription("DEBUG_OR_NOT_DEBUG", "Is debug mode enabled", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDescription.ParamType.BOOLEAN));
        propertyRepository.registerProperty(new PropertyDescription("TTL_PPL_ZZB_MGG", "Server timeout (in millis)", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.INTEGER));
        propertyRepository.registerProperty(new PropertyDescription("DNskfjsadkfasjdflsafj", "I don't know what property it must be", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.FLOAT));

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
