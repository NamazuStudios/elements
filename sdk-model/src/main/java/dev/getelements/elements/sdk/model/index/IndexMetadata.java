package dev.getelements.elements.sdk.model.index;

import java.util.Objects;

/**
 * Represents an implementation-specific type of index metadata.
 *
 * Instances implementing this interface must honor {@link Object#hashCode()} and {@link Object#equals(Object)} if
 * they refer to the same index.
 *
 * @param <IdentifierT>
 */
public interface IndexMetadata<IdentifierT> {

    /**
     * Gets the database-specific identifer for the index.
     * @return the identifier
     */
    IdentifierT getIdentifier();

}
