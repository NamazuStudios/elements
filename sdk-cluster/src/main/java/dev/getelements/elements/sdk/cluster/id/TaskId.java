package dev.getelements.elements.sdk.cluster.id;


import dev.getelements.elements.sdk.cluster.id.exception.InvalidResourceIdException;
import dev.getelements.elements.sdk.cluster.id.exception.InvalidTaskIdException;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

import static dev.getelements.elements.sdk.cluster.id.V1CompoundId.Field.*;

/**
 * Represents a globally-unique id of a task, associated with a resource.  This is currently backed by an
 * instance of {@link UUID}, but the string representation should be considered opaque by users of this type.
 *
 * Though the ResourceId is may be globally represented by a single UUID, for addressment we represent a TaskId with a
 * 4-tuple of UUIDs: (InstanceUuid, ApplicationUuid, ResourceUuid, TaskUuid). I.e. there may multiple Tasks for a
 * Resource, multiple Resources for an Application, multiple Applications for an Instance, and multiple Instances in a
 * deployment.
 *
 * By convention, we may represent the TaskId as a compound Id string, combining the string representation of the
 * {@link ResourceId} with the string representation of the TaskId's UUID, separated by the ID_SEPARATOR. Such a
 * string will take the form "{instance_uuid}.{app_uuid}+{resource_uuid}:{task_uuid}".
 */
public class TaskId implements Serializable, HasNodeId, HasCompoundId<V1CompoundId>  {

    private static final int SIZE = new TaskId(new V1CompoundId.Builder()
            .with(INSTANCE, UUID.randomUUID())
            .with(RESOURCE, UUID.randomUUID())
            .with(APPLICATION, UUID.randomUUID())
            .with(TASK, UUID.randomUUID())
            .build()).asBytes().length;

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile byte[] bytes;

    private transient volatile String string;

    private transient volatile NodeId nodeId;

    private transient volatile ResourceId resourceId;

    private TaskId() { v1CompoundId = null; }

    TaskId(final V1CompoundId v1CompoundId) {
        try {
            this.v1CompoundId = new V1CompoundId.Builder()
                    .with(v1CompoundId)
                    .only(INSTANCE, APPLICATION, RESOURCE, TASK)
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidResourceIdException(ex);
        }
    }

    /**
     * Creates a new unique {@link TaskId}.
     */
    public TaskId(final ResourceId resourceId) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(resourceId.v1CompoundId)
                    .with(TASK, UUID.randomUUID())
                    .only(INSTANCE, APPLICATION, RESOURCE, TASK)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidTaskIdException(ex);
        }
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asString()}.
     *
     * @param stringRepresentation the string representation
     */
    public TaskId(final String stringRepresentation) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(stringRepresentation)
                    .only(INSTANCE, APPLICATION, RESOURCE, TASK)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidTaskIdException(ex);
        }
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asBytes()}.
     *
     * @param byteRepresentation the string representation
     */
    public TaskId(final byte[] byteRepresentation) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(byteRepresentation)
                    .only(INSTANCE, APPLICATION, RESOURCE, TASK)
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidTaskIdException(ex);
        }
    }

    @Override
    public V1CompoundId getId() {
        return v1CompoundId;
    }

    /**
     * Returns the {@link NodeId} attached to this {@link TaskId}.
     *
     * @return the {@link NodeId}
     */
    @Override
    public NodeId getNodeId() {
        return nodeId == null ? (nodeId = new NodeId(v1CompoundId)) : nodeId;
    }

    /**
     * Returns the {@link ResourceId} attached to this {@link TaskId}
     *
     * @return the {@link ResourceId} attached to this {@link TaskId}
     */
    public ResourceId getResourceId() {
        return resourceId == null ? (resourceId = new ResourceId(v1CompoundId)) : resourceId;
    }

    /**
     * Returns the {@link byte[]} representation of this {@link TaskId}
     * @return
     */
    public byte[] asBytes() {
        return bytes == null ? (bytes = v1CompoundId.asBytes(INSTANCE, APPLICATION, RESOURCE, TASK)) : bytes;
    }

    /**
     * Returns the string representation of this {@link TaskId}
     *
     * @return the string representation
     */
    public String asString() {
        return string == null ? (string = v1CompoundId.asEncodedString(INSTANCE, APPLICATION, RESOURCE, TASK)) : string;
    }

    /**
     * Writes this {@link TaskId} to the supplied {@link ByteBuffer}.
     *
     * @param byteBuffer the {@link ByteBuffer} to receive the {@link TaskId}
     */
    public void toByteBuffer(final ByteBuffer byteBuffer) {
        v1CompoundId.toByteBuffer(byteBuffer, INSTANCE, APPLICATION, RESOURCE, TASK);
    }

    /**
     * Writes this {@link TaskId} to a {@link ByteBuffer} at the supplied position. As the position is specified,
     * this does not affect the buffer's mark, limit, position.
     *
     * @param byteBuffer the byteBuffer to receive the {@link TaskId}
     * @param position the position at which to write the byte buffer
     */
    public void toByteBuffer(final ByteBuffer byteBuffer, int position) {
        v1CompoundId.toByteBuffer(byteBuffer, position, INSTANCE, APPLICATION, RESOURCE, TASK);
    }

    /**
     * Reads a {@link TaskId} from the supplied {@link ByteBuffer} and position, ensuring that the buffer's mark, limit,
     * and position are unaffected.
     *
     * @param byteBufferRepresentation the byte buffer to read
     * @param byteBufferPosition the position of the node id within the byte buffer
     * @return the {@link TaskId} instance
     */
    public static TaskId taskIdFromByteBuffer(
            final ByteBuffer byteBufferRepresentation,
            final int byteBufferPosition) {
        return new TaskId(new V1CompoundId.Builder()
                .with(byteBufferRepresentation, byteBufferPosition, SIZE)
                .only(INSTANCE, APPLICATION, RESOURCE, TASK)
                .build());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!TaskId.class.equals(o.getClass())) return false;
        final TaskId taskId = (TaskId) o;
        return v1CompoundId.equals(taskId.v1CompoundId, INSTANCE, APPLICATION, RESOURCE, TASK);
    }

    @Override
    public int hashCode() {
        return hash == 0 ? (hash = v1CompoundId.hashCode(INSTANCE, APPLICATION, RESOURCE, TASK)) : hash;
    }

    @Override
    public String toString() {
        return asString();
    }

    /**
     * Gets the size of the {@link TaskId} in bytes.
     *
     * @return the size, in bytes
     */
    public static int getSizeInBytes() {
        return SIZE;
    }

}
