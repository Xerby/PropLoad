**Get property dictionary from YAML**
To enable the library to work with properties, they must first be registered. This can be achieved by either creating a
property using a class
and invoking the register method or by generating a YAML file and loading it using one of the static methods of the
class. You can see an examples of a [yaml file](/src/test/resources/example.yaml) in test directory, as well as an
example
of generating a file programmatically
in [SharedTestCommand.java](/src/test/java/ru/xerby/propload/SharedTestCommands.java) file.

The `PropertyDefinition` class comprises the following properties: `description`, `defaultValue`, 
`parametrization`, `isRequired`, `isSensitive`, `paramType`, `cmdAliases`, `charCmdAlias`, and `name`. 
You can instantiate the `PropertyDefinition` class either using the constructor by defining all the properties 
or using one of the helper methods, such as `createParameterlessProperty`, `createKeyValueRequiredProperty`,
`createSensitiveProperty`, or `createKeyValueOptionalProperty`.
Examples of creating properties using these methods can be found in the [`SharedTestCommands`](../src/test/java/ru/xerby/propload/SharedTestCommands.java) test class.

The second approach involves creating a YAML file with the necessary properties and loading it either from resources or
from a third-party file.
A sample YAML file with properties looks something like this:

```Yaml
#Oracle
DB_USERNAME:
  required: false
  param_type: STRING
  parametrization: PARAMETER_REQUIRED
DB_PASSWORD:
  required: true
  sensitive: true
  param_type: STRING
  parametrization: PARAMETER_REQUIRED
# Encoding server
OUTPUT_DIRECTORY:
  description: Path to the directory where the encoded files will be saved
  required: true
  param_type: STRING
  parametrization: PARAMETER_REQUIRED
  cmd_aliases: [ output, out ]
  char_cmd_alias: o
SERVER_PORT:
  required: false
  sensitive: false
  default_value: 8080
  param_type: INTEGER
  parametrization: PARAMETER_REQUIRED
  cmd_aliases: [ port ]
  char_cmd_alias: p
#Application
DELAYED:
  description: If we should delay the encoding process
  required: false
  parametrization: PARAMETER_PROHIBITED
```

All this is necessary so that the program further knows which properties can, in principle, be used and what should be
contained in them.

The library loads properties from several sources, including environment variables. Most likely, most of the environment
variables are not related to this program

Properties can be categorized as either "parameterized" or "unparameterized." For instance, when you call "program_name
--hey --delay 10m,"
"hey" is an unparameterized property, where the only concern is its presence or absence. In contrast, "delay" is a
parameterized property,
and "10m" is a value associated with it. The degree of parameterization for each property is specified using the "
parametrization" parameter, which can be set to one of three values:
"PARAMETER_REQUIRED", "PARAMETER_PROHIBITED", or "PARAMETER_OPTIONAL" (default).

Properties can be categorized as either "required" or "optional," with the "required" parameter determining their
status.
If a property is marked as required and is missing, it will throw an exception. In contrast, optional properties will be
ignored if they are not present.

Properties can be marked as "sensitive," meaning that their values should not be displayed in logs or other output.
Sensitive properties are often used for passwords, API keys, and other confidential information. When using 
PropertyLoader.toString() method it shows the value of sensitive properties as "***".

Properties can also have default values. If a property has a default value defined and still lacks a value after loading
from all sources,
it will be set to the default value.

It's important to note that a property cannot simultaneously be "required" and have a default value, as this is
contradictory.
Similarly, a property cannot have a default value and be parameterless, as this combination does not make sense.

When a property can have a parameter, its type can be specified (paramType property). By default, all parameters are
considered strings,
but you can define them as BOOLEAN, INTEGER, or FLOAT. During loading, the property undergoes additional validation to
ensure it contains
a fitting value based on the specified type.

Another important feature is the "cmdAliases" value. It provides an alternative way to specify a property on the command
line. For example,
if a parameter has the name "reinstall-dependencies" and "cmdAliases" is set to "redependence" and "dependencies" you
can set this
property in environment variables,
properties files, and other sources using the name "reinstall-dependencies." On the command line, you can use any of the
option
"--reinstall-dependencies", "--redependence" or "--dependencies" to set the property.

To declare such an array in a Yaml file, you can use one of two ways: either write each option on a new line, starting
with a hyphen followed by a space.
Or write all options separated by commas in square brackets.

```Yaml
main_username:
  cmd_aliases:
    - user
    - username
    - name
  default_value: me
  parametrization: PARAMETER_REQUIRED
```

or

```Yaml
main_username:
  cmd_aliases: [ user, username, name ]
  default_value: me
  parametrization: PARAMETER_REQUIRED
```

Additionally, a one-letter shortcut can be established using the "charCmdAlias" property. For instance, if it is set
to "d,"
you can use not only "--reinstall-dependencies", "--redependence" or "--dependencies" but also "-d" to set the property.

**Loading Property Definitions**

Get started with the library by creating
a [`PropertyDictionary`](../src/main/java/ru/xerby/propload/PropertyDictionary.java)
where property definitions will be stored. You can easily load data from a YAML file using one of the following static
methods:

- `PropertyDictionary loadFromResourceFile(String fileName, boolean caseSensitive)`
- `PropertyDictionary loadFromFile(File file, boolean caseSensitive)`

By default, parameter names are treated as case-insensitive. In other words, "DB_USERNAME," "db_username," and "
Db_UserName" are considered the same parameter.
However, this behavior can be modified by passing the corresponding parameter to a static method.

Loading a YAML file from the previous paragraph results in a PropertyDictionary containing five PropertyDefinitions.
Each [`PropertyDefinition`](../src/main/java/ru/xerby/propload/PropertyDefinition.java) stores a complete description of
a property,
including its name, whether it's required, whether it has a default value, and more.

Each PropertyDefinition has a name. You don’t have to specify it in the yaml file, it will be taken from the name of the
root,
but you can specify it manually, as required by the yaml standard.

```yaml
DB_USERNAME:
  name: db_username
  parametrization: PARAMETER_REQUIRED
DB_PASSWORD:
  name: db_password
  required: true
```

Sometimes it can be problematic to specify in advance the path to the Yaml file from which the settings will be loaded,
because this will impose restrictions on the user’s folder structure. To bypass this limitation, by default it is
supported to override the yml file
using the property-file key on the command line or the property-file environment variable.

**Loading Properties**

To load properties, you need to create and configure an instance of
the [`PropertyLoader`](../src/main/java/ru/xerby/propload/PropertyLoader.java) class.
The `PropertyLoader` is responsible for loading, validating, and consolidating properties from various sources
based on the settings and data from the provided `PropertyRepository`. First create `PropertyLoader` passing
a `PropertyRepository` to its constructor.

`PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);`

Then you can configure it to allow or deny properties specified in a Windows-style command line (e.g., /key),
set whether property values can be specified without an equal sign (e.g., "key value"), and more.

When you finish configuring the `PropertyLoader`, call the `buildProperties` method.

`public void buildProperties(String[] commandLineArgs, String externalPropertyFilePath, String envPropertyPrefix, String resourceName)`

This method takes command line arguments, an external (not located within the resources folder) property file,
an environment variable prefix, and a resource name to collect properties from all these sources. The sources are
prioritized,
with command line properties having the highest priority, followed by external files, environment variables, resource
properties files,
and default parameter values (if specified). The envPropertyPrefix parameter ensures that only variables with the
specified prefix are considered,
minimizing the risk of conflicts with variables from other programs.

After this, you will be able to use the properties. You can get a Map<String, String> with all the properties at once
by calling the propertyLoader.getProperties() method, or you can take the properties one at a time. In the latter case,
you can not only take them as strings using the get(String name) method, but also get typed values using the getAsInt,
getAsDouble and getAsBoolean methods.

You also can use toString method to see the properties were loaded in the PropertyLoader. It will show all the properties
names and values except the sensitive ones. If the property is sensitive, the value will be replaced with "***".

**Example**
An example of working with the library:

```java

@Data //lombok's "Data" annotation generates getters and setters for all fields and all-args constructor
public class Settings {
    private final String username;
    private final String password;
    private final String databaseUrl;
    private final String databaseName;
    private final String ffmpegFile;
    private final String outputDirectory;
    private final int maxFileSize;
    private final int serverPort;

    public static Settings loadApplicationProperties(String[] args) {
        PropertyDictionary propertyRepository = PropertyDictionary.loadFromResource("properties.yaml", false);

        PropertyLoader propertyLoader = new PropertyLoader(propertyRepository);

        propertyLoader.setThrowExceptionIfPropertyResourceNotFound(false);
        String externalPropFilename = new File("encodingServlet.properties").getAbsolutePath();
        String internalPropFilename = "application.properties";

        propertyLoader.buildProperties(args, externalPropFilename, "encserv.", internalPropFilename);

        Map<String, String> properties = propertyLoader.getProperties();

        int maxFileSize = propertyLoader.getAsInt("max_file_size");
        int serverPort = propertyLoader.getAsInt("server_port");
        String outputDirectory = properties.get("output_directory");
        if (outputDirectory.charAt(outputDirectory.length() - 1) != '/')
            outputDirectory += "/";

        return new Settings(properties.get("db_username"), properties.get("db_password"), properties.get("db_url"), properties.get("db_name"),
                properties.get("ffmpeg_file"), outputDirectory, maxFileSize, serverPort);
    }
}
```