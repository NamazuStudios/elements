package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents the globally unique ID for a particular {@link Resource}.  This is currently backed by
 * an instance of {@Link UUID}, but the string representation should be considered opaque by users
 * of this type.
 *
 * Created by patricktwohig on 4/11/17.
 */
public class ResourceId implements Serializable {

    private final UUID uuid;

    /**
     * Creates a new unique {@link ResourceId}.
     */
    public ResourceId() {
        uuid = UUID.randomUUID();
    }

    /**
     * Returns the string representation of this {@link ResourceId}
     *
     * @return the string representation
     */
    public String asString() {
        return uuid.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceId)) return false;

        ResourceId resourceId = (ResourceId) o;

        return uuid.equals(resourceId.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

}
