package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ElementServiceImplementationRecord;
import dev.getelements.elements.sdk.record.ElementServiceRecord;

/**
 * A DTO record for Element Service Metadata.
 * @param implementation the implementation
 * @param export the export
 */
public record ElementServiceMetadata(ElementServiceImplementationMetadata implementation,
                                     ElementServiceExportMetadata export) {

    /**
     * Convenience method to construct an ElementServiceMetadata from an ElementServiceRecord.
     * @param elementServiceRecord the ElementServiceRecord
     * @return  the newly created ElementServiceMetadata
     */
    public static ElementServiceMetadata from(final ElementServiceRecord elementServiceRecord) {
        return new ElementServiceMetadata(
                ElementServiceImplementationMetadata.from(elementServiceRecord.implementation()),
                ElementServiceExportMetadata.from(elementServiceRecord.export())
        );
    }

}
