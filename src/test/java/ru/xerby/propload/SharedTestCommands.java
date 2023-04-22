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

        return propertyRepository;
    }
}
