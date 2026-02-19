package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ElementDependencyRecord;

/**
 * A DTO record for Element Dependency Metadata. Contains only serializable representations of the
 * dependency, replacing the raw {@link dev.getelements.elements.sdk.annotation.ElementDependency}
 * annotation and {@link java.util.function.BiPredicate} held by {@link ElementDependencyRecord}.
 *
 * @param value the dependency selector value (typically the name of the required {@link dev.getelements.elements.sdk.Element})
 * @param selector the fully-qualified class name of the selector used to resolve the dependency
 */
public record ElementDependencyMetadata(String value, String selector) {

    /**
     * Constructs an {@link ElementDependencyMetadata} from an {@link ElementDependencyRecord}.
     *
     * @param record the source record
     * @return a new {@link ElementDependencyMetadata}
     */
    public static ElementDependencyMetadata from(final ElementDependencyRecord record) {
        return new ElementDependencyMetadata(
                record.dependency().value(),
                record.dependency().selector().getName()
        );
    }

}
