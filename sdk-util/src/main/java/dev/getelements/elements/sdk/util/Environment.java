package dev.getelements.elements.sdk.util;

/**
 * Constants for environment variables.
 */
public class Environment {

    private Environment() {}

    /**
     * The ELEMENTS_HOME environment variable.
     */
    public static final String ELEMENTS_HOME = "ELEMENTS_HOME";
    /**
     * The default elements configuration directory.
     */
    public static final String ELEMENTS_HOME_DEFAULT = "/opt/elements";
    /**
     * The ELEMENTS_TEMP environment variable.
     */
    public static final String ELEMENTS_TEMP = "ELEMENTS_TEMP";
    /**
     * Environment variable to indicate whether or not temporary files should be automatically purged.
     */
    public static final String ELEMENTS_TEMP_PURGE = "ELEMENTS_TEMP_PURGE";
    /**
     * Default for {@link #ELEMENTS_TEMP_PURGE}.
     */
    public static final String ELEMENTS_TEMP_PURGE_DEFAULT = "true";
    /**
     * Specifies the default temporary directory.
     */
    public static final String ELEMENTS_TEMP_DEFAULT = "tmp";

}
