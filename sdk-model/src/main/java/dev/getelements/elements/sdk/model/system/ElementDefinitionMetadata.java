package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementPackageRecord;
import dev.getelements.elements.sdk.record.ElementRecord;

import java.util.List;

/**
 * Record type for the {@link dev.getelements.elements.sdk.record.ElementDefinitionRecord}. This contains a summary of
 * all the metadata in an {@link ElementRecord} but only types which can be serialized (no ClassLoaders, etc).
 *
 * @param name the name of the {@link dev.getelements.elements.sdk.Element}
 * @param recursive true if recursive
 * @param additionalPackages additional packages to be included
 * @param loader the name of the laoder type
 */
public record ElementDefinitionMetadata(String name,
                                        boolean recursive,
                                        List<ElementPackageRecord> additionalPackages,
                                        String loader) {

    /**
     * Convenience method to construct an ElementDefinitionMetadata from an {@link ElementDefinitionRecord}.
     *
     * @param elementDefinition the {@link ElementDefinitionRecord}
     * @return the newly created {@link ElementDefinitionMetadata}
     */
    public static ElementDefinitionMetadata from(final ElementDefinitionRecord elementDefinition) {
        return new ElementDefinitionMetadata(
                elementDefinition.name(),
                elementDefinition.recursive(),
                elementDefinition.additionalPackages(),
                elementDefinition.loader().getName()
        );
    }

}
