package com.namazustudios.socialengine.rt.id;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.InvalidResourceIdException;

import java.io.Serializable;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.V1CompoundId.Field.*;

/**
 * Represents the globally unique ID for a particular {@link Resource}.  This is currently backed by
 * an instance of {@Link UUID}, but the string representation should be considered opaque by users
 * of this type. Though the ResourceId is may be globally represented by a single UUID, for addressment we represent
 * a ResourceId as a 3-tuple of UUIDs: (InstanceUuid, ApplicationUuid, ResourceUuid), i.e. Resource r is belongs to
 * Application a on Instance i. The first two elements of the tuple are represented in the code by a {@link NodeId}
 * owned by a {@link ResourceId} object, and the latter element would be held in a direct UUID object by the
 * {@link ResourceId} object.
 *
 * For now, a {@link ResourceId} should at all times have exactly both the NodeId and ResourceUuid assigned and
 * non-null.
 *
 * By convention, we may represent the ResourceId as a compound Id string, combining the string representation of the
 * {@link NodeId} with the string representation of the resource UUID, separated by the ID_SEPARATOR. Such a
 * string will take the form "{instance_uuid}.{app_uuid}+{resource_uuid}".
 *
 * Created by patricktwohig on 4/11/17.
 */
public class ResourceId implements Serializable, HasNodeId {

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile byte[] bytes;

    private transient volatile String string;

    private transient volatile NodeId nodeId;

    private ResourceId() { v1CompoundId = null; }

    /**
     * Creates a new unique {@link ResourceId}.
     */
    public ResourceId(final NodeId nodeId) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(nodeId.v1CompoundId)
                    .with(RESOURCE, UUID.randomUUID())
                    .only(INSTANCE, APPLICATION, RESOURCE)
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidResourceIdException(ex);
        }
    }

    /**
     * Parses a new {@link ResourceId} from the given {@link String}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param stringRepresentation the {@link String} representation of the {@link ResourceId} from {@link ResourceId#asString()}.
     */
    public ResourceId(final String stringRepresentation) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(stringRepresentation)
                    .only(INSTANCE, APPLICATION, RESOURCE)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidResourceIdException(ex);
        }
    }

    /**
     * Parses a new {@link ResourceId} from the given {@link byte[]}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param byteRepresentation the  {@link byte[]} representation of the {@link ResourceId} from {@link ResourceId#asBytes()}.
     */
    public ResourceId(final byte[] byteRepresentation) {
        v1CompoundId = new V1CompoundId.Builder()
                .with(byteRepresentation)
                .only(INSTANCE, APPLICATION, RESOURCE)
                .build();
    }

    /**
     * Implementation detail constructor.
     *
     * @param v1CompoundId the {@link V1CompoundId}
     */
    ResourceId(final V1CompoundId v1CompoundId) {
        try {
            this.v1CompoundId = new V1CompoundId.Builder()
                    .with(v1CompoundId)
                    .without(TASK)
                    .only(INSTANCE, APPLICATION, RESOURCE)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidResourceIdException(ex);
        }
    }

    /**
     * Returns the byte[] representation of this {@link ResourceId}
     *
     * @return the string representation
     */
    public byte[] asBytes() {
        return bytes == null ? (bytes = v1CompoundId.asBytes(INSTANCE, APPLICATION, RESOURCE)) : bytes;
    }

    /**
     * Returns the string representation of this {@link ResourceId}
     *
     * @return the string representation
     */
    public String asString() {
        return string == null ? (string = v1CompoundId.asString(INSTANCE, APPLICATION, RESOURCE)) : string;
    }

    /**
     * Returns the {@link NodeId} assocaited with this {@link ResourceId}.
     *
     * @return the {@link NodeId}
     */
    @Override
    public NodeId getNodeId() {
        return nodeId == null ? (nodeId = new NodeId(v1CompoundId)) : nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!ResourceId.class.equals(o.getClass())) return false;
        final ResourceId other = (ResourceId)o;
        return v1CompoundId.equals(other.v1CompoundId, INSTANCE, APPLICATION, RESOURCE);
    }

    @Override
    public int hashCode() {
        return hash == 0 ? (hash = v1CompoundId.hashCode(INSTANCE, APPLICATION, RESOURCE)) : hash;
    }

    @Override
    public String toString() {
        return asString();
    }

}
