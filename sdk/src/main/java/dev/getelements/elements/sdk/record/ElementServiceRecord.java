package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementService;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;

import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Objects.requireNonNull;

/**
 * Record type for {@link ElementService} annotation
 *
 * @param export the exported types
 * @param implementation the implementation record
 */
public record ElementServiceRecord(ElementServiceImplementationRecord implementation, ElementServiceExportRecord export) {

    /**
     * Gets a {@link Stream} of {@link ElementServiceRecord}s from the supplied {@link Class}
     *
     * @param aClass the type bearing the annotation
     *
     * @return a {@link Stream} of {@link ElementServiceRecord} instances, one for each of the defined annotations
     */
    public static Stream<ElementServiceRecord> fromClass(final Class<?> aClass) {

        requireNonNull(aClass, "aClass");

        final var elementServiceExports = aClass.getAnnotationsByType(ElementServiceExport.class);

        return Stream
                .of(elementServiceExports)
                .map(elementServiceExport -> ElementServiceExportRecord.fromClassAndExport(
                        aClass,
                        elementServiceExport
                ))
                .map(elementServiceExportRecord -> new ElementServiceRecord(
                        ElementServiceImplementationRecord.from(aClass),
                        elementServiceExportRecord
                ));

    }

    /**
     * Gets a {@link Stream} of all exposed types.
     *
     * @return a {@link Stream} of all exposed types.
     */
    public Stream<Class<?>> exposedTypes() {
        return export().exposed().stream();
    }

    /**
     * Gets the {@link ElementServiceRecord}s from the supplied {@link Package}
     *
     * @param aPackage {@link Package} with the {@link ElementService} annotation
     *
     * @return a {@link Stream} of {@link ElementServiceRecord} instances, one for each of the defined annotations
     */
    public static Stream<ElementServiceRecord> fromPackage(final Package aPackage) {

        final var elementServices = aPackage.getAnnotationsByType(ElementService.class);

        return Stream
                .of(elementServices)
                .map(elementService -> {

                    final var elementServiceExportRecord = ElementServiceExportRecord.fromClassAndExport(
                            elementService.value(),
                            elementService.export()
                    );

                    final var implementation = ElementServiceImplementationRecord.from(elementService.implementation());

                    return new ElementServiceRecord(implementation, elementServiceExportRecord);

                });

    }

}
