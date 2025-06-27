package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementSpiImplementation;
import dev.getelements.elements.sdk.exception.SdkException;

import java.util.List;
import java.util.stream.Stream;

/**
 * SPI implementation record for Element SPI implementations, this is constructed from the
 * {@link ElementSpiImplementation} annotation.
 * @param implementation the implementation class of the SPI
 * @param dependencies the set of dependencies that the SPI implementation requiress
 */
public record ElementSpiImplementationRecord(Class<?> implementation, List<ElementPackageRecord> dependencies) {

    public ElementSpiImplementationRecord
    {
        dependencies = List.copyOf(dependencies);
    }

    /**
     * Checks if the supplied {@link Class} is part of the SPI implementation.
     **/
    public boolean isPartOfElement(final Class<?> aClass) {
        return implementation().equals(aClass) || dependencies()
                .stream()
                .anyMatch(dep -> dep.isPartOfElement(aClass.getPackage()));
    }

    public static ElementSpiImplementationRecord from(final Class<?> implementation) {
        final var annotation = implementation.getAnnotation(ElementSpiImplementation.class);

        if (annotation == null) {
            throw new SdkException(
                    implementation.getName() + " " +
                    "does not have an ElementSpiImplementation annotation."
            );
        }

        final var dependencies = Stream.of(annotation.dependencies())
                .map(ElementPackageRecord::from)
                .toList();

        return new ElementSpiImplementationRecord(implementation, dependencies);

    }

}
