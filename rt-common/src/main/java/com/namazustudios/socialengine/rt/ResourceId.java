package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the globally unique ID for a particular {@link Resource}.  This is currently backed by
 * an instance of {@Link UUID}, but the string representation should be considered opaque by users
 * of this type. Though the ResourceId is may be globally represented by a single UUID, for addressment we represent
 * a ResourceId as a 3-tuple of UUIDs: (InstanceUuid, ApplicationUuid, ResourceUuid), i.e. Resource r is belongs to
 * Application a on Instance i. The first two elements of the tuple are represented in the code by a {@link WorkerId}
 * owned by a {@link ResourceId} object, and the latter element would be held in a direct UUID object by the
 * {@link ResourceId} object.
 *
 * For now, a {@link ResourceId} should at all times have exactly both the WorkerId and ResourceUuid assigned and
 * non-null.
 *
 * By convention, we may represent the ResourceId as a compound Id string, combining the string representation of the
 * {@link WorkerId} with the string representation of the resource UUID, separated by the ADDRESS_SEPARATOR. Such a
 * string will take the form "{instance_uuid}.{app_uuid}+{resource_uuid}".
 *
 * Created by patricktwohig on 4/11/17.
 */
public class ResourceId implements Serializable, InstanceUuidProvider {

    /**
     * Should not conflict with the {@link WorkerId#ADDRESS_SEPARATOR}.
     *
     * May be utilized in the Path string, in which case we should try to adhere to a valid URI schema. See:
     * https://stackoverflow.com/a/3641782.
     */
    public static final String ADDRESS_SEPARATOR = "\\+";

    public static final int WORKER_COMPOUND_ID_STRING_INDEX = 0;

    public static final int RESOURCE_UUID_STRING_INDEX = 1;

    private final WorkerId workerId;

    private final UUID resourceUuid;

    /**
     * Creates a new unique {@link ResourceId}.
     */
    public ResourceId(final WorkerId workerId) {
        this.workerId = workerId;
        resourceUuid = UUID.randomUUID();
    }

    /**
     * Parses a new {@link ResourceId} from the given {@link String}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param compoundIdString the {@link String} representation of the {@link ResourceId} from {@link ResourceId#asString()}.
     */
    public ResourceId(final String compoundIdString) {
        this.workerId = workerIdFromResourceCompoundIdString(compoundIdString);
        this.resourceUuid = resourceUuidFromCompoundIdString(compoundIdString);
    }

    /**
     * Returns the string representation of this {@link ResourceId}
     *
     * @return the string representation
     */
    public String asString() {
        return workerId.toString() + ADDRESS_SEPARATOR + resourceUuid.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceId that = (ResourceId) o;
        return Objects.equals(workerId, that.workerId) &&
                Objects.equals(resourceUuid, that.resourceUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, resourceUuid);
    }

    @Override
    public String toString() {
        return asString();
    }

    // TODO: combine these two string parser ops into a single op
    public static WorkerId workerIdFromResourceCompoundIdString(final String resourceCompoundIdString) {
        final int separatorCount = resourceCompoundIdString.length() - resourceCompoundIdString.replace(ADDRESS_SEPARATOR, "").length();
        if (separatorCount != 1) {
            throw new IllegalArgumentException("Resource ID string should have exactly one address separator: " + ADDRESS_SEPARATOR);
        }

        final String [] components = resourceCompoundIdString.split(ADDRESS_SEPARATOR);

        if (components.length != 2) {
            throw new IllegalArgumentException("Resource ID string could not be parsed successfully.");
        }

        final String workerCompoundIdString = components[WORKER_COMPOUND_ID_STRING_INDEX];

        final WorkerId workerId = new WorkerId(workerCompoundIdString);

        return workerId;
    }

    public static UUID resourceUuidFromCompoundIdString(final String resourceCompoundIdString) {
        final int separatorCount = resourceCompoundIdString.length() - resourceCompoundIdString.replace(ADDRESS_SEPARATOR, "").length();
        if (separatorCount != 1) {
            throw new IllegalArgumentException("Resource ID string should have exactly one address separator (@).");
        }

        final String [] components = resourceCompoundIdString.split(ADDRESS_SEPARATOR);

        if (components.length != 2) {
            throw new IllegalArgumentException("Resource ID string could not be parsed successfully.");
        }

        final String resourceUuidString = components[RESOURCE_UUID_STRING_INDEX];

        final UUID resourceUuid = UUID.fromString(resourceUuidString);

        return resourceUuid;
    }

    public UUID getInstanceUuid() {
        if (workerId != null) {
            return workerId.getInstanceUuid();
        }
        else {
            return null;
        }
    }
}
