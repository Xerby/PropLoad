package ru.xerby.propload;

public class SharedTestCommands {

    public static PropertyRepository createTestPropertyRepository() {
        PropertyRepository propertyRepository = new PropertyRepository();

        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("DB_PATH", "Path to database"));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("DB_USER", "Database user"));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueRequiredProperty("DB_PASSWORD", "Database password"));
        propertyRepository.registerProperty(PropertyDescription.createKeyValueOptionalProperty("SERVER_URL", "SR2E server URL"));
        propertyRepository.registerProperty(PropertyDescription.createParameterlessProperty("DELAYED",
                "Program will show main window not right away after run, but in a minute (suitable for use with autorun, so as not to annoy the user with the appearance of the program even before the operating system is fully loaded)"));
        propertyRepository.registerProperty(PropertyDescription.createParameterlessProperty("SCHEDULED",
                "Program will show main window not right away after run, but check schedule and show only if time is right (suitable use with autorun)"));
        propertyRepository.registerProperty(new PropertyDescription("DelayTime", "Value for delay after program starts", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_OPTIONAL, false, PropertyDescription.ParamType.STRING));
        propertyRepository.registerProperty(new PropertyDescription("DEBUG", "Is debug mode enabled", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, true, PropertyDescription.ParamType.BOOLEAN));
        propertyRepository.registerProperty(new PropertyDescription("TTL", "Server timeout (in millis)", null,
                PropertyDescription.ParametrizationDegree.PARAMETER_REQUIRED, false, PropertyDescription.ParamType.INTEGER));
        propertyRepository.registerProperty(new PropertyDescription("DN", "I don't know what property it must be", null,
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
}
