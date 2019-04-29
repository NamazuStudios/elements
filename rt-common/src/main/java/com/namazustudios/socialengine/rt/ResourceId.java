package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the globally unique ID for a particular {@link Resource}.  This is currently backed by
 * an instance of {@Link UUID}, but the string representation should be considered opaque by users
 * of this type.
 *
 * Created by patricktwohig on 4/11/17.
 */
public class ResourceId implements Serializable {

    public static final String ADDRESS_SEPARATOR = "@";

    public static final int NODE_UUID_INDEX = 0;

    public static final int RESOURCE_UUID_INDEX = 1;

    private final UUID nodeUuid;

    private final UUID resourceUuid;

    /**
     * Creates a new unique {@link ResourceId}.
     */
    public ResourceId(final UUID nodeUuid) {
        this.nodeUuid = nodeUuid;
        resourceUuid = UUID.randomUUID();
    }

    /**
     * Parses a new {@link ResourceId} from the given {@link String}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param compoundIdString the {@link String} representation of the {@link ResourceId} from {@link #asString()}.
     */
    public ResourceId(final String compoundIdString) {
        this.nodeUuid = nodeUuidFromCompoundIdString(compoundIdString);
        this.resourceUuid = resourceUuidFromCompoundIdString(compoundIdString);
    }

    /**
     * Returns the string representation of this {@link ResourceId}
     *
     * @return the string representation
     */
    public String asString() {
        return resourceUuid.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceId that = (ResourceId) o;
        return Objects.equals(nodeUuid, that.nodeUuid) &&
                Objects.equals(resourceUuid, that.resourceUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeUuid, resourceUuid);
    }

    @Override
    public String toString() {
        return asString();
    }

    public static UUID nodeUuidFromCompoundIdString(final String compoundIdString) {
        final int separatorCount = compoundIdString.length() - compoundIdString.replace(ADDRESS_SEPARATOR, "").length();
        if (separatorCount != 1) {
            throw new IllegalArgumentException("Resource ID string should have exactly one address separator (@).");
        }

        final String [] uuids = compoundIdString.split(ADDRESS_SEPARATOR);

        if (uuids.length != 2) {
            throw new IllegalArgumentException("Resource ID string could not be parsed successfully.");
        }

        final String nodeUuidString = uuids[NODE_UUID_INDEX];

        final UUID nodeUuid = UUID.fromString(nodeUuidString);

        return nodeUuid;
    }

    public static UUID resourceUuidFromCompoundIdString(final String compoundIdString) {
        final int separatorCount = compoundIdString.length() - compoundIdString.replace(ADDRESS_SEPARATOR, "").length();
        if (separatorCount != 1) {
            throw new IllegalArgumentException("Resource ID string should have exactly one address separator (@).");
        }

        final String [] uuids = compoundIdString.split(ADDRESS_SEPARATOR);

        if (uuids.length != 2) {
            throw new IllegalArgumentException("Resource ID string could not be parsed successfully.");
        }

        final String resourceUuidString = uuids[RESOURCE_UUID_INDEX];

        final UUID resourceUuid = UUID.fromString(resourceUuidString);

        return resourceUuid;
    }
}
