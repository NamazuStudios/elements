package dev.getelements.elements.sdk.guice;

import com.google.inject.Stage;

/**
 * Resolves the {@link Stage} a Guice injector should be built with from an operator-controlled system
 * property or environment variable, rather than hardcoding one. Every {@code Guice.createInjector(...)}
 * call site in the platform (the jetty-ws server, per-Element injectors, the {@code migrate}/{@code setup}
 * CLI tools, etc.) should use {@link #get()} instead of passing a fixed {@link Stage}, so that
 * {@link Stage#PRODUCTION}'s eager singleton construction and upfront binding validation stay strictly
 * opt-in: the default is the slower, but safer for iteration, {@link Stage#DEVELOPMENT}, and only a real
 * server deployment that explicitly sets the property/environment variable gets {@link Stage#PRODUCTION}.
 */
public final class GuiceStages {

    /**
     * The system property checked first. Value must match a {@link Stage} enum name, case-insensitively.
     */
    public static final String STAGE_PROPERTY = "dev.getelements.elements.guice.stage";

    /**
     * The environment variable checked if {@link #STAGE_PROPERTY} isn't set.
     */
    public static final String STAGE_ENV_VAR = "ELEMENTS_GUICE_STAGE";

    private GuiceStages() {}

    /**
     * Resolves the {@link Stage} to use for a Guice injector from, in priority order, the
     * {@value #STAGE_PROPERTY} system property, then the {@value #STAGE_ENV_VAR} environment variable,
     * defaulting to {@link Stage#DEVELOPMENT} if neither is set or the value doesn't match a known stage.
     *
     * @return the resolved {@link Stage}
     */
    public static Stage get() {

        final var raw = System.getProperty(STAGE_PROPERTY, System.getenv(STAGE_ENV_VAR));

        if (raw == null || raw.isBlank()) {
            return Stage.DEVELOPMENT;
        }

        try {
            return Stage.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Stage.DEVELOPMENT;
        }

    }

}
