package com.namazustudios.socialengine.config;

import com.namazustudios.socialengine.rt.Constants;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.Constants.*;
import static java.lang.String.format;
import static java.lang.System.getProperties;
import static java.lang.System.getenv;
import static java.util.stream.Collectors.toMap;

/**
 * Implements the default configuration scheme.  In addition to providing a set of {@link Properties}
 * that has a set of "out of the box" defaults, this will attempt to load properties from either
 * system properties or a properties file defined by the system properties.
 *
 * Created by patricktwohig on 5/9/17.
 */
public class DefaultConfigurationSupplier implements Supplier<Properties> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurationSupplier.class);

    private static final char PROPERTY_SEPARATOR = '.';

    private static final char ENVIRONMENT_SEPARATOR = '_';

    private static final String PROPERTY_PREFIX = "com.namazustudios";

    private static final String ENVIRONMENT_PROPERTY_PREFIX = PROPERTY_PREFIX.replace(PROPERTY_SEPARATOR, ENVIRONMENT_SEPARATOR);

    private static final String ENVIRONMENT_PREFIX = "ELEMENTS";

    private final Properties properties;

    private final Properties defaultProperties;

    /**
     * Uses all default configuration.  This scans the classpath using the {@link ClassLoader} from
     * {@link ClassLoader#getSystemClassLoader()} to scan for defaults and will load properties according to the default
     * configuration {@see {@link #loadProperties()}}.
     */
    public DefaultConfigurationSupplier() {
        this(loadProperties());
    }

    /**
     * Loads and returns properties using a variety of fallbacks.  This first attempts to read from the default the
     * default properties file.  If no such file exists, this will use the value of {@link System#getProperties()} as
     * the application configuration.
     *
     * The order of properties loading is as follows, meaning that items farther down in the list overwrite the ones
     * above it.
     *
     * <pre>
     * <ul>
     *     <li>All Environment variables</li>
     *     <li>All JVM System Properties</li>
     *     <li>All values from the file $ELEMENTS_HOME/conf/elements-configuration.properties</li>
     *     <li>One of the following:
     *         <ul>
     *             <li>ENV com.namazustudios.socialengine.configuration.properties</li>
     *             <li>-D com.namazustudios.socialengine.configuration.properties</li>
     *             <li>The file in the current working directory "socialengine-configuration.properties"</li>
     *         </ul>
     *     </li>
     *     <li>
     *         <ul>
     *             <li>ENV com.namazustudios.elements.configuration.properties</li>
     *             <li>-D com.namazustudios.elements.configuration.properties</li>
     *             <li>The file in the current working directory "elements-configuration.properties"</li>
     *         </ul>
     *     </li>
     * </ul>
     * </pre>
     *
     * @return the {@link Properties} used to configure the application.
     */
    public static Properties loadProperties() {

        // The priority of the following is really important.

        final var env = getenv()
            .entrySet()
            .stream()
            .filter(DefaultConfigurationSupplier::shouldKeepEnvironmentVariable)
            .collect(toMap(DefaultConfigurationSupplier::remapEnvironmentVariable, Entry::getValue));

        final var properties = new Properties();

        properties.putAll(env);
        properties.putAll(getProperties());

        final var home = env.getOrDefault(Constants.ELEMENTS_HOME, Constants.ELEMENTS_HOME_DEFAULT);

        return loadProperties(
            properties,

            // ELEMENTS_HOME/config/elements-configuration.properties
            Paths.get(home, CONFIGURATION_DIRECTORY, DEFAULT_PROPERTIES_FILE),

            // Loads the old-style configuration file
            Paths.get(properties.getProperty(PROPERTIES_FILE_OLD, DEFAULT_PROPERTIES_FILE_OLD)),

            // Loads the new-style configuration file
            Paths.get(properties.getProperty(PROPERTIES_FILE, DEFAULT_PROPERTIES_FILE))

        );

    }

    private static boolean shouldKeepEnvironmentVariable(final Entry<String, String> stringStringEntry) {

        final var name = stringStringEntry.getKey();

        return name.startsWith(ENVIRONMENT_PREFIX) ||
               name.toLowerCase().startsWith(PROPERTY_PREFIX) ||
               name.toLowerCase().startsWith(ENVIRONMENT_PROPERTY_PREFIX);

    }

    private static String remapEnvironmentVariable(final Entry<String, String> environmentEntry) {

        final var original = environmentEntry.getKey();

        return original.startsWith(ENVIRONMENT_PROPERTY_PREFIX)
            ? original.toLowerCase().replace(ENVIRONMENT_SEPARATOR, PROPERTY_SEPARATOR)
            : original;

    }

    /**
     * Loads and accumulates multiple {@link Paths} containing the contents {@link Properties}.
     *
     * @param properties the initial {@link Properties}
     * @param paths all paths to load
     * @return the {@link Properties} loaded from each path
     */
    public static Properties loadProperties(final Properties properties, final Path... paths) {

        final var result = new Properties();
        result.putAll(properties);

        for (var path : paths) {
            final var loaded = load(path.toFile());
            result.putAll(loaded);
        }

        return result;

    }

    private static Properties load(final File propertiesFile) {

        final Properties loaded = new Properties();

        try (final var is = new FileInputStream(propertiesFile)) {
            loaded.load(is);
            logger.info("Loaded properties from file: {}", propertiesFile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            logger.info("Could not find {}. Skipping.", propertiesFile.getAbsolutePath());
        } catch (IOException ex) {
            logger.warn("Could not load properties from {}. Skipping.", propertiesFile.getAbsolutePath(), ex);
        }

        return loaded;

    }

    /**
     * This scans the classpath using the {@link ClassLoader} from {@link ClassLoader#getSystemClassLoader()} to scan
     * for defaults and will use the supplied properties.  The configured properties are loaded using
     * {@link #loadProperties()}
     * @param properties the
     */
    public DefaultConfigurationSupplier(final Properties properties) {
        this(ClassLoader.getSystemClassLoader(), properties);
    }

    public DefaultConfigurationSupplier(final ClassLoader classLoader) {
        this(classLoader, loadProperties());
    }

    public DefaultConfigurationSupplier(final ClassLoader classLoader, final Properties properties) {

        defaultProperties = scanForDefaults(classLoader);
        this.properties = new Properties(defaultProperties);
        this.properties.putAll(properties);

        final var sb = new StringBuilder();

        final Predicate<Entry<Object, Object>> filter = e ->
            e.getKey().toString().startsWith(PROPERTY_PREFIX) ||
            e.getKey().toString().startsWith(ENVIRONMENT_PREFIX);

        sb.append("\nApplication Properties:\n");
        properties
            .entrySet()
            .stream()
            .filter(filter)
            .forEach(e -> sb.append(format("\t%s=%s\n", e.getKey(), e.getValue())));

        sb.append("Default Properties:\n");
        defaultProperties.forEach((k, v) -> sb.append(format("\t%s=%s\n", k, v)));

        sb.append("System Properties (Included in Application Properties):\n");
        properties
            .entrySet()
            .stream()
            .filter(filter.negate())
            .forEach(e -> sb.append(format("\t%s=%s\n", e.getKey(), e.getValue())));

        logger.info("{}\n", sb.toString());
    }

    @Override
    public Properties get() {
        final Properties properties = new Properties(defaultProperties);
        properties.putAll(this.properties);
        return properties;
    }

    private Properties scanForDefaults(final ClassLoader classLoader) {

        final var reflections = new Reflections("com.namazustudios", classLoader);
        final var classSet = reflections.getSubTypesOf(ModuleDefaults.class);

        final Properties defaultProperties = new Properties();

        for (final Class<? extends ModuleDefaults> cls : classSet) {
            try {

                logger.info("Loading default properties for {}", cls);

                final ModuleDefaults defaults = cls.newInstance();
                defaultProperties.putAll(defaults.get());

            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Could not build module defaults.", e);
            }
        }

        return defaultProperties;

    }

    /**
     * Returns a copy of the loaded system default properties.
     *
     * @return the default properties.
     */
    public Properties getDefaultProperties() {
        final Properties defaultProperties = new Properties();
        defaultProperties.putAll(this.defaultProperties);
        return defaultProperties;
    }

}
