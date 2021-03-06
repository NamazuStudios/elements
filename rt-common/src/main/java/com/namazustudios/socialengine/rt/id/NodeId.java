package com.namazustudios.socialengine.rt.id;

import com.namazustudios.socialengine.rt.exception.InvalidNodeIdException;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Optional;
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
public class NodeId implements Serializable, HasNodeId {

    private static final int SIZE = new NodeId(new V1CompoundId.Builder()
            .with(INSTANCE, UUID.randomUUID())
            .with(APPLICATION, UUID.randomUUID())
        .build()).asBytes().length;

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile byte[] bytes;

    private transient volatile String string;

    private transient volatile InstanceId instanceId;

    private transient volatile ApplicationId applicationId;

    private NodeId() { v1CompoundId = null; }

    /**
     * Implementation detail.  This is used by other IDs to properly instantiate the id.
     *
     * @param v1CompoundId
     */
    NodeId(final V1CompoundId v1CompoundId) {
        try {
            this.v1CompoundId = new V1CompoundId.Builder()
                    .with(v1CompoundId)
                    .without(TASK, RESOURCE)
                    .only(INSTANCE, APPLICATION)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidNodeIdException(ex);
        }
    }

    /**
     * Gets the {@link UUID} for this {@link NodeId}.
     *
     * @return the {@link UUID}
     */
    public ApplicationId getApplicationId() {
        return (applicationId == null) ? (applicationId = new ApplicationId(v1CompoundId)) : applicationId;
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
        return string == null ? (string = v1CompoundId.asEncodedString(INSTANCE, APPLICATION)) : string;
    }

    /**
     * Represents this {@link NodeId} as a set of packed-bytes.
     *
     * @return the bytes of this {@link NodeId}.
     */
    public byte[] asBytes() {
        return (bytes == null ? (bytes = v1CompoundId.asBytes(INSTANCE, APPLICATION)) : bytes).clone();
    }

    /**
     * Writes this {@link NodeId} to a {@link ByteBuffer} at the supplied position. The {@link NodeId} will be placed
     * at {@link ByteBuffer#position()}. The supplied buffer must have at least {@link #getSizeInBytes()} bytes
     * remaining.
     *
     * @param byteBuffer the byteBuffer to receive the {@link NodeId}
     */
    public void toByteBuffer(final ByteBuffer byteBuffer) {
        v1CompoundId.toByteBuffer(byteBuffer, INSTANCE, APPLICATION);
    }

    /**
     * Writes this {@link NodeId} to a {@link ByteBuffer} at the supplied position. As the position is specified, this
     * does not affect the buffer's mark, limit, position.
     *
     * @param byteBuffer the byteBuffer to receive the {@link NodeId}
     * @param position the position at which to write the byte buffer
     */
    public void toByteBuffer(final ByteBuffer byteBuffer, int position) {
        v1CompoundId.toByteBuffer(byteBuffer, position, INSTANCE, APPLICATION);
    }

    @Override
    public NodeId getNodeId() throws InvalidNodeIdException {
        return this;
    }

    @Override
    public Optional<NodeId> getOptionalNodeId() {
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!NodeId.class.equals(o.getClass())) return false;
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

    /**
     * Returns the number of bytes when a {@link NodeId} is stored as a {@link byte[]}
     *
     * @return the size, in bytes
     */
    public static int getSizeInBytes() {
        return SIZE;
    }

    /**
     * Generates a completely random {@link NodeId}.  Used mostly for testing.
     *
     * @return a newly constructued {@link NodeId}
     */
    public static NodeId randomNodeId() {
        return new NodeId(new V1CompoundId.Builder()
                .with(INSTANCE, UUID.randomUUID())
                .with(APPLICATION, UUID.randomUUID())
                .build()
        );
    }

    /**
     * Creats a {@link NodeId} that is for a master node.  By convention, the master node is the node whose instance
     * id matches the application ID.  This makes it possible to get information about the rest of the hosted nodes
     * by only knowing the instance ID.
     *
     * @param instanceId the instance ID.
     *
     * @return the {@link NodeId} for the instance
     */
    public static NodeId forMasterNode(final InstanceId instanceId) {
        try {

            final V1CompoundId v1CompoundId = new V1CompoundId.Builder()
                    .with(instanceId.v1CompoundId)
                    .with(APPLICATION, instanceId.v1CompoundId.getComponent(INSTANCE).getValue())
                    .only(INSTANCE, APPLICATION)
                .build();

            return new NodeId(v1CompoundId);

        } catch (IllegalArgumentException ex) {
            throw new InvalidNodeIdException(ex);
        }
    }

    /**
     * Constructs a {@link NodeId} for the given {@link InstanceId} as well as {@link ApplicationId}.
     *
     * @param instanceId the {@link InstanceId}
     * @param applicationId the {@link ApplicationId}
     * @return a new {@link NodeId}
     */
    public static NodeId forInstanceAndApplication(final InstanceId instanceId, final ApplicationId applicationId) {
        try {
            return new NodeId(new V1CompoundId.Builder()
                .with(instanceId.v1CompoundId)
                .with(applicationId.v1CompoundId.getComponent(APPLICATION))
                .only(INSTANCE, APPLICATION)
                .build()
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidNodeIdException(ex);
        }
    }

    /**
     * Parses a new {@link NodeId} from the given {@link String}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param stringRepresentation the {@link String} representation of the {@link NodeId} from {@link NodeId#asString()}.
     */
    public static NodeId nodeIdFromString(final String stringRepresentation) {
        try {
            return new NodeId(new V1CompoundId.Builder()
                    .with(stringRepresentation)
                    .only(INSTANCE, APPLICATION)
                .build()
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidNodeIdException(ex);
        }
    }

    /**
     * Parses a new {@link NodeId} from the given {@link byte[]}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param byteRepresentation the  {@link byte[]} representation of the {@link NodeId} from {@link NodeId#asBytes()}.
     */
    public static NodeId nodeIdFromBytes(final byte[] byteRepresentation) {
        return new NodeId(new V1CompoundId.Builder()
                .with(byteRepresentation)
                .only(INSTANCE, APPLICATION)
            .build()
        );
    }

    /**
     * Reads a {@link NodeId} from the supplied {@link ByteBuffer}. The buffer's position will be advanced by the
     * size of the {@link NodeId}.
     *
     * @param byteBufferRepresentation the byte buffer to read
     * @return the {@link NodeId} instance
     */
    public static NodeId nodeIdFromByteBuffer(final ByteBuffer byteBufferRepresentation) {

        final var oldLimit = byteBufferRepresentation.limit();
        final var newLimit = byteBufferRepresentation.position() + getSizeInBytes();

        try {
            return new NodeId(new V1CompoundId.Builder()
                    .with(byteBufferRepresentation.limit(newLimit))
                    .only(INSTANCE, APPLICATION, RESOURCE)
                    .build());
        } finally {
            byteBufferRepresentation.limit(oldLimit);
        }

    }

    /**
     * Reads a {@link NodeId} from the supplied {@link ByteBuffer} and position, ensuring that the buffer's mark, limit,
     * and position are unaffected.
     *
     * @param byteBufferRepresentation the byte buffer to read
     * @param byteBufferPosition the position of the node id within the byte buffer
     * @return the {@link NodeId} instance
     */
    public static NodeId nodeIdFromByteBuffer(final ByteBuffer byteBufferRepresentation, int byteBufferPosition) {
        return new NodeId(new V1CompoundId.Builder()
                .with(byteBufferRepresentation, byteBufferPosition, SIZE)
                .only(INSTANCE, APPLICATION)
            .build());
    }

}
