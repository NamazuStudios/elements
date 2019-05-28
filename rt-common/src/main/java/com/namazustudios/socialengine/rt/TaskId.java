package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Represents a globally-unique id of a task, associated with a {@link Resource}.  This is currently backed by an
 * instance of {@Link UUID}, but the string representation should be considered opaque by users of this type.
 *
 * By convention, we may represent the TaskId as a compound Id string, combining the string representation of the
 * {@link ResourceId} with the string representation of the TaskId's UUID, separated by the ID_SEPARATOR. Such a
 * string will take the form "{instance_uuid}.{app_uuid}+{resource_uuid}:{task_uuid}".
 */
public class TaskId implements Serializable, AddressAliasProvider {

    public static final String ID_SEPARATOR = ":";

    public static final Pattern ID_SEPARATOR_PATTERN = Pattern.compile(ID_SEPARATOR);

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

        final String[] components = ID_SEPARATOR_PATTERN.split(stringRepresentation);

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
        return format("%s%s%s", resourceId.asString(), ID_SEPARATOR, uuid.toString());
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof TaskId)) return false;
        TaskId taskId = (TaskId) object;
        return Objects.equals(uuid, taskId.uuid) &&
               Objects.equals(getResourceId(), taskId.getResourceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, getResourceId());
    }

    public UUID getAddressAlias() {
        return resourceId.getAddressAlias();
    }

}
