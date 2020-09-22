package com.namazustudios.socialengine.rt.id;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.InvalidTaskIdException;

import java.io.Serializable;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.V1CompoundId.Field.*;
import static java.lang.String.format;

/**
 * Represents a globally-unique id of a task, associated with a {@link Resource}.  This is currently backed by an
 * instance of {@Link UUID}, but the string representation should be considered opaque by users of this type.
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
public class TaskId implements Serializable, HasNodeId {

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile byte[] bytes;

    private transient volatile String string;

    private transient volatile NodeId nodeId;

    private transient volatile ResourceId resourceId;

    private TaskId() { v1CompoundId = null; }

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

}
