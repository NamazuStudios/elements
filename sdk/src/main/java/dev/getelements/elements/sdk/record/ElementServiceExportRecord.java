package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A record type for the {@link ElementServiceExport}.
 *
 * @param exposed all exposed types
 * @param name the name of the service
 */
public record ElementServiceExportRecord(List<Class<?>> exposed, String name) {

    public ElementServiceExportRecord {
        name = name == null ? "" : name;
        exposed = List.copyOf(exposed);
    }

    /**
     * Creates a {@link ElementServiceExportRecord} from the supplied {@link Class<?>}.
     *
     * @param serviceClass the implemenation type
     * @return the {@link ElementServiceRecord}
     */
    public static ElementServiceExportRecord fromClass(final Class<?> serviceClass) {

        final var elementServiceImplementation = serviceClass.getAnnotation(ElementServiceExport.class);

        if (elementServiceImplementation == null) {
            throw new IllegalArgumentException("Element service implementation is not annotated with @ElementServiceImplementation");
        }

        return fromClassAndExport(serviceClass, elementServiceImplementation);

    }

    /**
     * Indicates if the exported service is named.
     *
     * @return true, if named, false otherwise
     */
    public boolean isNamed() {
        return !name.isEmpty();
    }

    /**
     * Creates a {@link ElementServiceExportRecord} from the supplied {@link Class} and
     * {@link ElementServiceExport}
     *
     * @param aClass the type bearing the annotation
     * @return the {@link ElementServiceRecord}
     */
    public static ElementServiceExportRecord fromClassAndExport(
            final Class<?> aClass,
            final ElementServiceExport elementServiceExport) {

        requireNonNull(aClass, "aClass");
        requireNonNull(elementServiceExport, "elementServiceExport");

        final var name = elementServiceExport.name().strip();

        final List<Class<?>> exposed = elementServiceExport.value().length == 0
                ? List.of(aClass)
                : List.of(elementServiceExport.value());

        return new ElementServiceExportRecord(exposed, name);

    }

}
