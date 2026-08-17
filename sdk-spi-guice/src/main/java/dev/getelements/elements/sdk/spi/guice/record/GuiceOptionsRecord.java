package dev.getelements.elements.sdk.spi.guice.record;

import dev.getelements.elements.sdk.spi.guice.annotations.GuiceOptions;

import static dev.getelements.elements.sdk.spi.guice.annotations.GuiceOptions.LoadingStrategy.LEGACY;

/**
 * A record type for {@link GuiceOptions}, resolved from a {@link Package} with the {@link GuiceOptions} annotation
 * defaulted to its documented defaults when the annotation is absent.
 *
 * @param strategy the {@link GuiceOptions.LoadingStrategy}
 * @param strict whether strict validation is enabled
 */
public record GuiceOptionsRecord(GuiceOptions.LoadingStrategy strategy, boolean strict) {

    /**
     * The default options used when a package bears no {@link GuiceOptions} annotation.
     */
    public static final GuiceOptionsRecord DEFAULT = new GuiceOptionsRecord(LEGACY, false);

    /**
     * Resolves the {@link GuiceOptionsRecord} for the supplied {@link Package}, falling back to {@link #DEFAULT}
     * if the package bears no {@link GuiceOptions} annotation.
     *
     * @param aPackage the {@link Package} which may bear the {@link GuiceOptions} annotation
     * @return the resolved {@link GuiceOptionsRecord}
     */
    public static GuiceOptionsRecord fromPackage(final Package aPackage) {
        final var guiceOptions = aPackage.getAnnotation(GuiceOptions.class);
        return guiceOptions == null
                ? DEFAULT
                : new GuiceOptionsRecord(guiceOptions.strategy(), guiceOptions.strict());
    }

}
