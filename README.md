**Library for Loading, Validating, and Managing Properties**

This library simplifies the handling of properties loaded from multiple sources. Whether you need to use property files
during development,
load parameters from environment variables in a staging server or Docker environment, or allow users to specify
parameter files
when running on a server, this library has you covered. It loads parameters from various sources, removes duplicates,
and presents them in the form of a simple map (parameter_name=value).

**Getting Started**

Begin by creating a `PropertyDictionary` where property definitions will be stored. You can easily load data from a YAML
file
using one of the following static methods:

- `PropertyDictionary loadFromResourceFile(String fileName, boolean caseSensitive)`
- `PropertyDictionary loadFromFile(File file, boolean caseSensitive)`

Here's an example of a [YAML file](/src/test/resources/example.yaml):

```yaml
# MongoDB
DB_USERNAME:
  required: true
DB_PASSWORD:
  required: true

# Encoding server
SERVER_PORT:
  description: Servlet port for incoming connections
  default_value: 8080
  param_type: INTEGER
  parametrization: PARAMETER_REQUIRED

# Application
MAX_FILE_SIZE:
  description: Maximum document size in bytes
  required: false
  param_type: INTEGER
  parametrization: PARAMETER_REQUIRED
  default_value: 10485760
```

Loading this YAML file results in a `PropertyDictionary` containing four `PropertyDefinition`s.
Each `PropertyDefinition` stores
a complete description of a property, including its name, whether it's required, whether it has a default value, and
more.

**Loading Properties**

To load properties, at first create an instance of the `PropertyLoader` class, passing a `PropertyDictionary` to it.
The `PropertyLoader` is responsible for loading, validating, and consolidating properties from various sources based on
the settings
and data from the provided `PropertyDictionary`. After creating a `PropertyLoader`, you can configure it to allow
properties
specified in a Windows-style command line (e.g., /key), set whether property values can be specified without an equal
sign (e.g., "key value"), and more.

`public void buildProperties(String[] commandLineArgs, String externalPropertyFilePath, String envPropertyPrefix, String resourceName)`

Then call the `buildProperties` method. This method takes command line arguments, an external  (not located within the
resources folder) property file,
an environment variable prefix, and a resource name to collect properties from all these sources. The sources are
prioritized, with command line properties
having the highest priority, followed by external files, environment variables, resource properties files, and default
parameter values (if specified).
The `envPropertyPrefix` parameter ensures that only variables with the specified prefix are considered, minimizing the
risk of conflicts with variables from other programs.

**Using Properties**

Once properties are loaded, you can access them by calling the `propertyLoader.getProperties()` method to get
a `Map<String, String>`
with all properties at once. Alternatively, you can access properties one at a time using methods
like `get(String name)`
or get typed values using `getAsInt`, `getAsDouble`, and `getAsBoolean` methods.

You can find much more detailed information in the file [Tutorial.md](doc/tutorial.md).