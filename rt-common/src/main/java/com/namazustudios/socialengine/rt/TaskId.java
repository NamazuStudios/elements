package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents a globally-unique id of a task, associated with a {@link Resource}.  This is currently backed by an
 * instance of {@Link UUID}, but the string representation should be considered opaque by users of this type.
 */
public class TaskId implements Serializable {

    public static final Pattern ID_SEPARATOR = Pattern.compile("/");

    private final UUID uuid;

    private ResourceId resourceId;

    /**
     * Creates a new unique {@link TaskId}.
     */
    public TaskId(final ResourceId resourceId) {
        uuid = UUID.randomUUID();
        this.resourceId = resourceId;
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asString()}.
     *
     * @param stringRepresentation the string representation
     */
    public TaskId(final String stringRepresentation) {

        final String[] components = ID_SEPARATOR.split(stringRepresentation);

        if (components.length != 2) {
            throw new IllegalArgumentException("Task id format invalid: " + stringRepresentation);
        }

        uuid = UUID.fromString(components[1]);
        resourceId = new ResourceId(components[0]);

    }

    /**
     * Returns the {@link ResourceId} attached to this {@link TaskId}
     *
     * @return the {@link ResourceId} attached to this {@link TaskId}
     */
    public ResourceId getResourceId() {
        return resourceId;
    }

    /**
     * Returns the string representation of this {@link TaskId}
     *
     * @return the string representation
     */
    public String asString() {
        return uuid.toString();
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskId)) return false;

        TaskId taskId = (TaskId) o;

        return uuid != null ? uuid.equals(taskId.uuid) : taskId.uuid == null;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

}
