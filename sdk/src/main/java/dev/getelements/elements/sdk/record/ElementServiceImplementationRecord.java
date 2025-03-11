package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementService;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation.DefaultImplementation;

/**
 * A record for the {@link ElementServiceImplementation} annotation.
 * @param type the implementation type
 * @param expose true if the implementation itself is exposed
 */
public record ElementServiceImplementationRecord(Class<?> type, boolean expose) {

    /**
     * A singleton constant default {@link ElementServiceImplementation}.
     */
    public static final ElementServiceImplementationRecord DEFAULT = new ElementServiceImplementationRecord(DefaultImplementation.class, false);

    /**
     * Checks if this is the {@link DefaultImplementation} implementation.
     *
     * @return boolean true if
     */
    public boolean isDefault() {
        return DefaultImplementation.class.equals(type());
    }

    /**
     * Generates an {@link ElementServiceImplementationRecord} from a {@link ElementServiceImplementation}.
     *
     * @param elementService the {@link ElementService}
     * @return the {@link ElementServiceImplementationRecord}
     */
    public static ElementServiceImplementationRecord from(final ElementService elementService) {
        final var implementation = elementService.implementation();
        return from(implementation);
    }

    /**
     * Generates the {@link ElementServiceImplementationRecord} from the supplied {@link Class}
     * bearing the {@link ElementServiceImplementation} annotation.
     *
     * @param aClass the {@link Class}
     * @return the {@link ElementServiceExportRecord}
     */
    public static ElementServiceImplementationRecord from(final Class<?> aClass) {

        final var elementServiceImplementation = aClass.getAnnotation(ElementServiceImplementation.class);

        if (elementServiceImplementation == null) {
            return DEFAULT;
        }

        final var record = from(elementServiceImplementation);

        return !record.isDefault()
                ? record
                : new ElementServiceImplementationRecord(aClass, false);

    }

    /**
     * Generates an {@link ElementServiceImplementationRecord} from a {@link ElementServiceImplementation}.
     *
     * @param elementServiceImplementation the {@link ElementServiceImplementation}
     * @return the {@link ElementServiceImplementationRecord}
     */
    public static ElementServiceImplementationRecord from(final ElementServiceImplementation elementServiceImplementation) {
        return new ElementServiceImplementationRecord(
                elementServiceImplementation.value(),
                elementServiceImplementation.expose()
        );
    }

}
