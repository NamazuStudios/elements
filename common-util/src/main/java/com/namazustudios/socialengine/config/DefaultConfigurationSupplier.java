package com.namazustudios.socialengine.config;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.Constants.DEFAULT_PROPERTIES_FILE;
import static com.namazustudios.socialengine.Constants.PROPERTIES_FILE;
import static java.lang.String.format;
import static java.lang.System.getProperties;

/**
 * Implements the default configuration scheme.  In addition to providing a set of {@link Properties}
 * that has a set of "out of the box" defaults, this will attempt to load properties from either
 * system properties or a properties file defined by the system properties.
 *
 * Created by patricktwohig on 5/9/17.
 */
public class DefaultConfigurationSupplier implements Supplier<Properties> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurationSupplier.class);

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
     * @return the {@link Properties} used to configure the application.
     */
    public static Properties loadProperties() {
        final File propertiesFile = new File(getProperties().getProperty(PROPERTIES_FILE, DEFAULT_PROPERTIES_FILE));

        final Properties properties = new Properties();

        try (final InputStream is = new FileInputStream(propertiesFile)) {
            final Properties loadedProperties = new Properties();
            loadedProperties.load(is);
            properties.putAll(loadedProperties);
            logger.info("Loaded properties from file: {}", propertiesFile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            properties.putAll(getProperties());
            logger.info("Could not find {}.  Using system properties.", propertiesFile.getAbsolutePath());
        } catch (IOException ex) {
            properties.putAll(getProperties());
            logger.warn("Could not load properties from {}.  Using system properties.", propertiesFile.getAbsolutePath(), ex);
        }

        return properties;
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

        final StringBuilder sb = new StringBuilder();
        sb.append("Application Properties:\n");
        properties.forEach((k, v) -> sb.append(format("\t%s=%s\n", k, v)));
        logger.info("{}\n", sb.toString());

        sb.append("Default Properties:\n");
        defaultProperties.forEach((k, v) -> sb.append(format("\t%s=%s\n", k, v)));
        logger.info("{}\n", sb.toString());

    }

    @Override
    public Properties get() {
        final Properties properties = new Properties(defaultProperties);
        properties.putAll(this.properties);
        return properties;
    }

    private Properties scanForDefaults(final ClassLoader classLoader) {

        final Reflections reflections = new Reflections("com.namazustudios", classLoader);
        final Set<Class<? extends ModuleDefaults>> classSet = reflections.getSubTypesOf(ModuleDefaults.class);

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

}
