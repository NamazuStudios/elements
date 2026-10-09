package dev.getelements.elements.sdk.util;

import java.util.Map.Entry;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;

/**
 * Resolves operator-supplied configuration from environment variables and JVM system properties, using the
 * same environment-variable translation scheme as the rest of the platform: an environment variable named
 * like {@code dev_getelements_elements_guice_stage} is translated to the dotted property key
 * {@code dev.getelements.elements.guice.stage}, and a plain {@code ELEMENTS_}-prefixed environment variable
 * is kept as-is under its own literal name. JVM system properties always take priority over an environment
 * variable that resolves to the same name.
 *
 * <p>This exists so that early bootstrap code -- code that runs before any Guice injector (and therefore
 * before a fully classpath-scanned configuration supplier) exists -- can resolve individual operator
 * settings using the exact same scheme as the rest of the platform's configuration, without depending on
 * the heavier, classpath-scanning module that builds the full application configuration.</p>
 */
public final class OperatorProperties {

    private static final char PROPERTY_SEPARATOR = '.';

    private static final char ENVIRONMENT_SEPARATOR = '_';

    private static final String PROPERTY_PREFIX = "dev.getelements";

    private static final String ENVIRONMENT_PROPERTY_PREFIX = PROPERTY_PREFIX.replace(PROPERTY_SEPARATOR, ENVIRONMENT_SEPARATOR);

    private static final String ENVIRONMENT_PREFIX = "ELEMENTS";

    private OperatorProperties() {}

    /**
     * Resolves operator-supplied properties from environment variables (translated as described above) and
     * JVM system properties, which take priority over environment variables resolving to the same name.
     *
     * @return the merged {@link Properties}
     */
    public static Properties resolve() {

        final var env = System.getenv()
                .entrySet()
                .stream()
                .filter(OperatorProperties::shouldKeepEnvironmentVariable)
                .collect(toMap(OperatorProperties::remapEnvironmentVariable, Entry::getValue));

        final var properties = new Properties();
        properties.putAll(env);
        properties.putAll(System.getProperties());

        return properties;

    }

    private static boolean shouldKeepEnvironmentVariable(final Entry<String, String> entry) {

        final var name = entry.getKey();

        return name.startsWith(ENVIRONMENT_PREFIX) ||
                name.toLowerCase().startsWith(PROPERTY_PREFIX) ||
                name.toLowerCase().startsWith(ENVIRONMENT_PROPERTY_PREFIX);

    }

    private static String remapEnvironmentVariable(final Entry<String, String> entry) {

        final var original = entry.getKey();

        return original.startsWith(ENVIRONMENT_PROPERTY_PREFIX)
                ? original.toLowerCase().replace(ENVIRONMENT_SEPARATOR, PROPERTY_SEPARATOR)
                : original;

    }

}
