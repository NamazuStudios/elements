package dev.getelements.elements.config;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.record.ElementDefaultAttributeRecord;
import dev.getelements.elements.sdk.util.Environment;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.model.Constants.*;
import static dev.getelements.elements.sdk.record.ElementDefaultAttributeRecord.REDACTED;
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

    private static final String PROPERTY_PREFIX = "dev.getelements";

    private static final String ENVIRONMENT_PROPERTY_PREFIX = PROPERTY_PREFIX.replace(PROPERTY_SEPARATOR, ENVIRONMENT_SEPARATOR);

    private static final String ENVIRONMENT_PREFIX = "ELEMENTS";

    private final Properties properties;

    private final Properties defaultProperties;

    private final List<ElementDefaultAttributeRecord> defaultAttributeRecords;

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
     * <ul>
     *     <li>All Environment variables</li>
     *     <li>All JVM System Properties</li>
     *     <li>All values from the file $ELEMENTS_HOME/conf/elements-configuration.properties</li>
     *     <li>One of the following:
     *         <ul>
     *             <li>ENV dev.getelements.elements.configuration.properties</li>
     *             <li>-D dev.getelements.elements.configuration.properties</li>
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

        final var home = env.getOrDefault(Environment.ELEMENTS_HOME, Environment.ELEMENTS_HOME_DEFAULT);

        return loadProperties(
            properties,

            // ELEMENTS_HOME/config/elements-configuration.properties
            Paths.get(home, CONFIGURATION_DIRECTORY, DEFAULT_PROPERTIES_FILE),

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
     * This scans the classpath using the {@link ClassLoader} from {@link Class#getClassLoader()} to scan
     * for defaults and will use the supplied properties.  The configured properties are loaded using
     * {@link #loadProperties()}
     * @param properties the
     */
    public DefaultConfigurationSupplier(final Properties properties) {
        final var classLoader = getClass().getClassLoader();

        defaultAttributeRecords = scanForDefaultAttributes(classLoader);
        defaultProperties = scanForDefaults(classLoader);
        this.properties = new Properties(defaultProperties);
        this.properties.putAll(properties);

        final var sb = new StringBuilder();

        final Predicate<Entry<Object, Object>> filter = e ->
            e.getKey().toString().startsWith(PROPERTY_PREFIX) ||
            e.getKey().toString().startsWith(ENVIRONMENT_PREFIX);

        final var redacted = defaultAttributeRecords
                .stream()
                .filter(ElementDefaultAttributeRecord::sensitive)
                .map(ElementDefaultAttributeRecord::name)
                .collect(Collectors.toSet());

        sb.append("\nApplication Properties:\n");
        properties
            .entrySet()
            .stream()
            .filter(filter)
            .forEach(e -> sb.append(format("\t%s=%s\n",
                    e.getKey(),
                    redacted.contains(e.getKey()) ? REDACTED : e.getValue())));

        sb.append("Default Properties:\n");
        defaultProperties
                .entrySet()
                .forEach(e -> sb.append(format("\t%s=%s\n",
                        e.getKey(),
                        redacted.contains(e.getKey()) ? REDACTED : e.getValue())));

        sb.append("System Properties (Included in Application Properties):\n");
        properties
            .entrySet()
            .stream()
            .filter(filter.negate())
            .forEach(e -> sb.append(format("\t%s=%s\n",
                    e.getKey(),
                    redacted.contains(e.getKey()) ? REDACTED : e.getValue())));

        sb.append("All Known Default Attributes:\n");

        defaultAttributeRecords
                .forEach(attr -> sb.append("\t%s=%s - %s\n".formatted(
                        attr.name(),
                        attr.sensitive() ? REDACTED : attr.value(),
                        attr.description()
                )));

        logger.info("{}\n", sb);

    }

    @Override
    public Properties get() {
        final Properties properties = new Properties(defaultProperties);
        properties.putAll(this.properties);
        return properties;
    }

    private List<ElementDefaultAttributeRecord> scanForDefaultAttributes(final ClassLoader classLoader) {

        final var result = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .ignoreParentClassLoaders()
                .enableClassInfo()
                .acceptPackages("dev.getelements")
                .enableFieldInfo()
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan();

        try (result) {

            return result.getClassesWithFieldAnnotation(ElementDefaultAttribute.class)
                    .stream()
                    .flatMap(classInfo -> classInfo
                            .getDeclaredFieldInfo()
                            .stream()
                            .filter(fieldInfo ->
                                    fieldInfo.hasAnnotation(ElementDefaultAttribute.class) &&
                                            fieldInfo.isStatic() &&
                                            fieldInfo.isFinal())
                            .map(FieldInfo::loadClassAndGetField)
                    )
                    .map(ElementDefaultAttributeRecord::from)
                    .toList();

        }

    }

    private Properties scanForDefaults(final ClassLoader classLoader) {

        final var result = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .ignoreParentClassLoaders()
                .enableClassInfo()
                .acceptPackages("dev.getelements")
                .enableFieldInfo()
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan();

        try (result) {

            final Properties defaultProperties = new Properties();
            defaultAttributeRecords.forEach(record -> defaultProperties.put(record.name(), record.value()));

            final var moduleDefaultClassInfo = result.getClassesImplementing(ModuleDefaults.class);

            for (final var classInfo : moduleDefaultClassInfo) {

                final var cls = classInfo.loadClass();

                try {

                    logger.info("Loading default properties for {}", cls);

                    final ModuleDefaults defaults = (ModuleDefaults) cls.getConstructor().newInstance();
                    defaultProperties.putAll(defaults.get());

                } catch (InstantiationException |
                         IllegalAccessException |
                         NoSuchMethodException |
                         InvocationTargetException e) {
                    logger.error("Could not build module defaults.", e);
                }

            }

            return defaultProperties;

        }

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
