package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a globally-unique id of a task, associated with a {@link Resource}.  This is currently backed by an
 * instance of {@Link UUID}, but the string representation should be considered opaque by users of this type.
 */
public class TaskId implements Serializable {

    private final UUID uuid;

    /**
     * Creates a new unique {@link TaskId}.
     */
    public TaskId() {
        uuid = UUID.randomUUID();
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asString()}.
     *
     * @param stringRepresentation the string representation
     */
    public TaskId(final String stringRepresentation) {
        uuid = UUID.fromString(stringRepresentation);
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
