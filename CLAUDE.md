# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PropLoad (`ru.xerby:propload`) is a small Java 11 **library** for loading, validating, and merging application
configuration properties from multiple sources. There is no main class or entry point — it is consumed as a Maven
dependency and published to GitHub Packages.

Property sources are merged by priority (highest wins):

**command-line args → external .properties file → environment variables (optionally prefix-filtered) → bundled resource
.properties file (default name `properties.properties`) → `default_value` from the property definition.**

A property is loaded only if it is declared in the `PropertyDictionary`; unknown properties are ignored or cause
exceptions depending on loader flags. A required property missing from all sources throws `IllegalArgumentException`.

## Commands

```
mvn test                                              # run all tests
mvn test -Dtest=PropertyLoaderTest                    # single test class
mvn test -Dtest=PropertyLoaderTest#LoaderToStringTest # single test method
mvn package                                           # build the jar
mvn deploy                                            # publish to GitHub Packages
```

Surefire is configured with `--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED`
because tests mutate environment variables via reflection (`system-rules` library). When running tests outside Maven (
e.g., an IDE run configuration), add the same VM options or the env-manipulating tests will fail.

## Architecture

Single package `ru.xerby.propload`, six classes. Two core concepts: a **property definition** (schema: what may/must be
provided and how) and a **property** (the resulting key→value pair).

Pipeline: `PropertyDictionary` (schema) → `PropertyLoader.buildProperties(...)` (load + validate + merge) → flat
`Map<String, String>` via `getProperties()`, plus typed getters
`get / getAsInt / getAsLong / getAsDouble / getAsBoolean`.

### PropertyDictionary — the schema

- `extends TreeMap<String, PropertyDefinition>`; the comparator makes keys **case-insensitive by default**. Case
  sensitivity is fixed at construction and is set *only* here — `PropertyLoader` inherits it from the dictionary.
- Built programmatically (`registerProperty`) or deserialized from YAML (`loadFromResource` / `loadFromFile` /
  `loadFromInputStream`) via Jackson + `YAMLFactory`.
- After YAML deserialization, `adjustNames()` fills each definition's `name` from its map key; an explicitly specified
  `name` must equal the key up to case and punctuation (alphanumeric characters must match), otherwise it throws.
- `getByCmdProperty(...)` resolves a parsed command-line token to a definition by name, any of `cmdAliases` (long
  aliases), or `charCmdAlias` (single-char `-x` key). Aliases work **only** on the command line; env vars and files
  always use the property name.

### PropertyDefinition — one property's schema

Fields: `description`, `defaultValue`, `parametrization`, `isRequired`, `isSensitive`, `paramType`, `cmdAliases`,
`charCmdAlias`, `name`.

- `parametrization` (`ParametrizationDegree`): `PARAMETER_PROHIBITED` (flag-like, no value), `PARAMETER_OPTIONAL` (
  default), `PARAMETER_REQUIRED`.
- `paramType` (`ParamType`): `STRING` (default) / `BOOLEAN` / `INTEGER` / `LONG` / `FLOAT`. Values are type-checked
  during loading (`checkValueType`), not only on typed getters.
- Constructor validation: `required` + `defaultValue` are mutually exclusive; `defaultValue` + `PARAMETER_PROHIBITED` is
  illegal.
- Parameterless properties are stored in the result map with a `null` value — presence is the signal. From non-cmd
  sources their raw value must be empty or boolean-truthy (`""`, `"true"`, `"1"`, `"yes"`, `"y"`, `"t"`).
- `isSensitive` values render as `***` in `PropertyLoader.toString()`.
- Static factories: `createParameterlessProperty`, `createKeyValueRequiredProperty`, `createKeyValueOptionalProperty`,
  `createSensitiveProperty`.

### PropertyLoader — loading and merging

- Constructed with a dictionary. `buildProperties(cmdArgs, externalPropertyFilePath, envPropertyPrefix, resourceName)`
  runs the whole pipeline. Higher-priority sources load first; later loads skip keys that are already present (first
  wins). Defaults and required-checks run last (`setDefaultIfIsNotSet`).
- ~9 boolean flags control strictness, all Javadoc'ed on the fields: `throwExceptionIfUnknownCmdPropertyFound` (true),
  `throwExceptionIfUnknownEnvPropertyFound` (true; only applies when a prefix is given),
  `throwExceptionIfUnknownPropFilePropertyFound` (false), `throwExceptionIfUnboundTokenFound` (true),
  `throwExceptionIfPropertyResourceNotFound` (true), `throwExceptionIfExternalPropertyFileNotFound` (true),
  `isParametrizedWithoutEqualSignAllowed` (true), `isEnabledWindowsKeyCompatibility` (false),
  `canRedefineExternalPropertyFile` (true).
- **`property-file` magic key**: unless `canRedefineExternalPropertyFile=false`, `buildProperties` auto-registers a
  `property-file` definition into the dictionary; the user can override the external properties file path via
  `--property-file` on the command line or the `<prefix>property-file` env var. The key is removed from the result map
  afterwards.
- `envPropertyPrefix` filters env vars and strips the prefix: with prefix `myapp.`, env var `myapp.DB_USER` becomes
  property `DB_USER`.

### Command-line parsing

`ParsedCmdProperties.parse(...)` and `ParsedCmdProperty` (both package-private) tokenize `--key=value`, `--key value`,
`--flag`, `-k` (single-char short key), and Windows-style `/key` (opt-in). `isSurelyParametrized` marks `=`-bound
values; whether a space-separated token binds as a value depends on `isParametrizedWithoutEqualSignAllowed`. Dangling
tokens throw or are logged per `throwExceptionIfUnboundTokenFound`.

`CmdUtilities` is a small public facade for using this parser standalone (`String[] args` → `Map<String, String>`)
without any dictionary or validation.

### YAML definition format

Jackson uses `SnakeCaseStrategy`, so YAML fields are snake_case versions of the Java fields: `default_value`,
`param_type`, `parametrization`, `required`, `sensitive`, `cmd_aliases`, `char_cmd_alias`, `name`. Sample:
`src/test/resources/example.yaml`; user-facing docs: `README.md` and `doc/tutorial.md`.

## Gotchas and conventions

- **Dependency versions are fixed** (plain versions in pom.xml properties; Dependabot opens monthly update PRs — see
  `.github/dependabot.yml`). Historical context: versions used to be Maven *ranges*, and a range is a hard constraint
  that overrides the Jackson version declared in consuming applications.
- YAML loading deserializes into a plain `LinkedHashMap` first and then copies into
  `new PropertyDictionary(caseSensitive)` — deserializing straight into `PropertyDictionary` would go through the no-arg
  constructor and silently drop the `caseSensitive` argument (regression tests: `loadCaseSensitive*` in
  `PropertyYamlTest`).
- Tests are **JUnit 4** (`org.junit.Assert`, `@Rule EnvironmentVariables` from
  `com.github.stefanbirkner:system-rules`) — not JUnit 5. Lombok is used throughout (`@Data`, `@Getter`,
  `@SneakyThrows`, `@Slf4j`, `@Synchronized`).
- `SharedTestCommands` holds shared test fixtures: factory methods for test dictionaries (default, case-sensitive,
  long-names, sensitive-data variants) and a temp `.properties` file generator.
- Naming drift in docs: the old name `PropertyRepository` still appears in README/tutorial and a test helper name — the
  actual class is `PropertyDictionary`; README mentions `loadFromResourceFile`, the actual method is `loadFromResource`.
  Keep `README.md` and `doc/tutorial.md` in sync when changing the public API.
- Keep code Java 11-compatible (`maven.compiler.source/target = 11`).
- Open TODOs in code comments (in Russian): combined short keys (`PropertyDefinition`), generating help/usage text from
  the dictionary (`PropertyDictionary`).
