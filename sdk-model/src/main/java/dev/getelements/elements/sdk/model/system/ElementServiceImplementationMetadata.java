package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ElementServiceImplementationRecord;

/**
 * A DTO record for Element Service Implementation Metadata.
 * @param type the type of the service implementation
 * @param expose true if the service implementation should be exposed
 */
public record ElementServiceImplementationMetadata(String type, boolean expose) {

    public static ElementServiceImplementationMetadata from(final ElementServiceImplementationRecord implementation) {
        return new ElementServiceImplementationMetadata(
                implementation.type().getName(),
                implementation.expose()
        );
    }

}
