package com.namazustudios.socialengine.model.gameon;

/**
 * Specifies the application build type when operating against a {@link GameOnSession} instance.
 */
public enum AppBuildType {

    /**
     * Development/testing/debug build.
     */
    development,

    /**
     * Production/release build.
     */
    release;

    /**
     * The default type, as a hardcoded string.  (Designated this way to be used with enum constants).
     */
    public static final String DEFAULT_TYPE_STRING = "release";

    /**
     * The default type when not specified.  Currently defaults to {@link #html} per Amazon's recommendation.
     *
     * @return the default OS type.
     */
    public static AppBuildType getDefault() {
        return release;
    }


}
