package com.namazustudios.socialengine.rt.id;

import sun.text.resources.en.FormatData_en_IE;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.V1CompoundId.Field.*;

/**
 * Represents a single worker in the deployment cluster, i.e. the globally-unique identifier for a given game/app
 * processor within a Java process. It is identified by a UUID pair (InstanceUuid, ApplicationUuid), where InstanceUuid
 * is the unique identifier for the ec2 instance, and ApplicationUuid is the application-specific id for the game/app.
 * E.g. if we have three games/apps represented by UUIDs A1, A2, A3, and the workload necessitates horizontal scaling
 * across two ec2 instances represented by UUIDs I1, I2, then we will have six workers in the deployment addressable
 * with the pairs (I1, A1), (I1, A2), (I1, A3), (I2, A1), (I2, A2), (I2, A3).
 */
public class NodeId implements Serializable {

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile String string;

    private transient volatile InstanceId instanceId;

    private NodeId() { v1CompoundId = null; }

    public NodeId(final InstanceId inaInstanceId, final UUID applicationId) {
        v1CompoundId = new V1CompoundId.Builder()
                .with(instanceId.v1CompoundId)
                .with(APPLICATION, applicationId)
                .only(INSTANCE, APPLICATION)
            .build();
    }


    public NodeId(final UUID instanceId, final UUID applicationId) {
        v1CompoundId = new V1CompoundId.Builder()
                .with(INSTANCE, instanceId)
                .with(APPLICATION, applicationId)
            .build();
    }

    /**
     * Parses a new {@link NodeId} from the given {@link String}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param stringRepresentation the {@link String} representation of the {@link NodeId} from {@link #asString()}.
     */
    public NodeId(final String stringRepresentation) {
        v1CompoundId = new V1CompoundId.Builder()
                .with(stringRepresentation)
                .only(INSTANCE, APPLICATION)
            .build();
    }

    /**
     * Implementation detail.  This is used by other IDs to properly instantiate the id.
     *
     * @param v1CompoundId
     */
    NodeId(final V1CompoundId v1CompoundId) {
        this.v1CompoundId = new V1CompoundId.Builder()
                .with(v1CompoundId)
                .without(TASK, RESOURCE)
                .only(INSTANCE, APPLICATION)
            .build();
    }

    /**
     * Gets the {@link UUID} for this {@link NodeId}.
     *
     * @return the {@link UUID}
     */
    public UUID getApplicationUuid() {
        return v1CompoundId.getComponent(APPLICATION).getValue();
    }

    /**
     * Returns the {@link InstanceId}.
     *
     * @return the {@link InstanceId} assocaited with this {@link NodeId}
     */
    public InstanceId getInstanceId() {
        return (instanceId == null) ? (instanceId = new InstanceId(v1CompoundId)) : instanceId;
    }

    /**
     * Returns the compound Id string representation of this {@link NodeId}
     *
     * @return the string representation
     */
    public String asString() {
        return string == null ? (string = v1CompoundId.asString(INSTANCE, APPLICATION)) : string;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (NodeId.class.equals(o.getClass())) return false;
        final NodeId other = (NodeId)o;
        return v1CompoundId.equals(other.v1CompoundId, INSTANCE, APPLICATION);
    }

    @Override
    public int hashCode() {
        return hash == 0 ? (hash = v1CompoundId.hashCode(INSTANCE, APPLICATION)) : hash;
    }

    @Override
    public String toString() {
        return asString();
    }

}
