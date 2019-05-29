package com.namazustudios.socialengine.rt;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single worker in the deployment cluster, i.e. the globally-unique identifier for a given game/app
 * processor within a Java process. It is identified by a UUID pair (InstanceUuid, ApplicationUuid), where InstanceUuid is the
 * unique identifier for the ec2 instance, and ApplicationUuid is the application-specific id for the game/app. E.g. if we
 * have three games/apps represented by UUIDs A1, A2, A3, and the workload necessitates horizontal scaling across two
 * ec2 instances represented by UUIDs I1, I2, then we will have six workers in the deployment addressable with the pairs
 * (I1, A1), (I1, A2), (I1, A3), (I2, A1), (I2, A2), (I2, A3).
 *
 * For now, a {@link NodeId} should at all times have exactly both of the UUIDs assigned and non-null.
 *
 * By convention, we may represent these address pairs in a single string of the form "{instance_uuid}.{app_uuid}",
 * separated by the String {@link NodeId#ID_SEPARATOR}. This is referred to as a compoundIdString. This module
 * provides convenience methods to construct that representation as well as instantiate a new {@link NodeId} from a
 * given string of that form.
 *
 */
public class NodeId implements Serializable, AddressAliasProvider {

    /**
     * Should not conflict with {@link ResourceId#ID_SEPARATOR}.
     *
     * May be utilized in the Path string, in which case we should try to adhere to a valid URI schema. See:
     * https://stackoverflow.com/a/3641782.
     */
    public static final String ID_SEPARATOR = ".";

    public static final int INSTANCE_UUID_STRING_INDEX = 0;

    public static final int APPLICATION_UUID_STRING_INDEX = 1;

    private final UUID instanceUuid;

    private final UUID applicationUuid;

    public NodeId(final UUID instanceUuid, final UUID applicationUuid) {
        this.instanceUuid = instanceUuid;
        this.applicationUuid = applicationUuid;
    }

    /**
     * Parses a new {@link NodeId} from the given {@link String}.  The should be the string representation returned
     * byt {@link #asString()}.
     *
     * @param compoundIdString the {@link String} representation of the {@link NodeId} from {@link #asString()}.
     */
    public NodeId(final String compoundIdString) {
        final int separatorCount = compoundIdString.length() - compoundIdString.replace(ID_SEPARATOR, "").length();
        if (separatorCount != 1) {
            throw new IllegalArgumentException("Worker ID string should have exactly one address separator: " + ID_SEPARATOR);
        }

        final String [] components = compoundIdString.split(ID_SEPARATOR);

        if (components.length != 2) {
            throw new IllegalArgumentException("Worker ID string could not be parsed successfully.");
        }

        final String instanceUuidString = components[INSTANCE_UUID_STRING_INDEX];
        this.instanceUuid = UUID.fromString(instanceUuidString);

        final String applicationUuidString = components[APPLICATION_UUID_STRING_INDEX];
        this.applicationUuid = UUID.fromString(applicationUuidString);
    }

    /**
     * Returns the compound Id string representation of this {@link NodeId}
     *
     * @return the string representation
     */
    public String asString() {
        final String compoundIdString = instanceUuid.toString() + ID_SEPARATOR + applicationUuid.toString();
        return compoundIdString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return Objects.equals(instanceUuid, nodeId.instanceUuid) &&
                Objects.equals(applicationUuid, nodeId.applicationUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceUuid, applicationUuid);
    }

    @Override
    public String toString() {
        return asString();
    }

    public UUID getInstanceUuid() {
        return instanceUuid;
    }

    public UUID getApplicationUuid() {
        return applicationUuid;
    }

    public UUID getAddressAlias() {
        return instanceUuid;
    }
}
