package com.namazustudios.socialengine.rt;

import java.util.UUID;

/**
 * Represents the globally unique ID for a particular {@link Resource}.
 *
 * Created by patricktwohig on 4/11/17.
 */
public class ResourceId {

    private final UUID uuid;

    public ResourceId() {
        uuid = UUID.randomUUID();
    }

    private String asString() {
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

}

