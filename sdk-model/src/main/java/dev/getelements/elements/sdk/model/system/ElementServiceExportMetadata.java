package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ElementServiceExportRecord;

import java.util.List;

/**
 * A DTO record for Element Service Export Metadata.
 * @param exposed the list of exposed types
 * @param name the name of the export
 */
public record ElementServiceExportMetadata(List<String> exposed, String name) {

    /**
     * Convenience method to construct an ElementServiceExportMetadata from an ElementServiceExportRecord.
     * @param export the ElementServiceExportRecord
     * @return the newly created ElementServiceExportMetadata
     */
    public static ElementServiceExportMetadata from(final ElementServiceExportRecord export) {
        return new ElementServiceExportMetadata(
                export.exposed() == null
                        ? List.of()
                        : export.exposed().stream()
                            .map(Class::getName)
                            .toList(),
                export.name()
        );
    }

}
