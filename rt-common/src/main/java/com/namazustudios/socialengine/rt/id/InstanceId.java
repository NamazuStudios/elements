package com.namazustudios.socialengine.rt.id;

import java.io.Serializable;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.V1CompoundId.Builder;
import static com.namazustudios.socialengine.rt.id.V1CompoundId.Field.*;
import static java.util.UUID.randomUUID;

public class InstanceId implements Serializable {

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile String string;

    public InstanceId() {
        v1CompoundId = new Builder()
                .with(INSTANCE, randomUUID())
            .build();
    }

    public InstanceId(final String stringRepresentation) {
        v1CompoundId = new Builder()
                .with(stringRepresentation)
                .only(INSTANCE)
            .build();
    }

    InstanceId(final V1CompoundId v1CompoundId) {
        this.v1CompoundId = new Builder()
                .with(v1CompoundId)
                .without(APPLICATION, RESOURCE, TASK)
                .only(INSTANCE)
            .build();
    }

    /**
     * Gets a {@link UUID} representing the instance id.
     *
     * @return the {@link UUID} of this {@link InstanceId}
     */
    public UUID getUuid() {
        return v1CompoundId.getComponent(INSTANCE).getValue();
    }

    /**
     * Returns the compound Id string representation of this {@link NodeId}
     *
     * @return the string representation
     */
    public String asString() {
        return string == null ? (string = v1CompoundId.asString(INSTANCE)) : string;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (InstanceId.class.equals(o.getClass())) return false;
        final InstanceId other = (InstanceId)o;
        return v1CompoundId.equals(other.v1CompoundId, INSTANCE);
    }

    @Override
    public int hashCode() {
        return hash == 0 ? (hash = v1CompoundId.hashCode(INSTANCE)) : hash;
    }

    @Override
    public String toString() {
        return asString();
    }

}
