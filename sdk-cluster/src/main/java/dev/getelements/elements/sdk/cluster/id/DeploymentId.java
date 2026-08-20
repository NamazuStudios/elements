package dev.getelements.elements.sdk.cluster.id;

import dev.getelements.elements.sdk.cluster.id.exception.InvalidDeploymentIdException;

import java.io.Serializable;
import java.util.UUID;

import static dev.getelements.elements.sdk.cluster.id.V1CompoundId.Field.*;
import static java.util.UUID.nameUUIDFromBytes;
import static java.util.UUID.randomUUID;

/**
 * Uniquely identifies a deployment.
 */
public class DeploymentId implements Serializable, HasCompoundId<V1CompoundId>  {

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile byte[] bytes;

    private transient volatile String string;

    private DeploymentId() { v1CompoundId = null; }

    DeploymentId(final V1CompoundId v1CompoundId) {
        try {
            this.v1CompoundId = new V1CompoundId.Builder()
                    .with(v1CompoundId)
                    .without(INSTANCE, RESOURCE, TASK)
                    .only(APPLICATION)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidDeploymentIdException(ex);
        }
    }

    /**
     * Creates a new unique {@link DeploymentId}.
     */
    public DeploymentId(final UUID deploymentUuid) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(APPLICATION, deploymentUuid)
                    .only(APPLICATION)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidDeploymentIdException(ex);
        }
    }

    /**
     * Creates the {@link DeploymentId} from the provided string representation, as obtained from {@link #asString()}.
     *
     * @param stringRepresentation the string representation
     */
    public DeploymentId(final String stringRepresentation) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(stringRepresentation)
                    .only(APPLICATION)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidDeploymentIdException(ex);
        }
    }

    /**
     * Creates the {@link DeploymentId} from the provided string representation, as obtained from {@link #asBytes()}.
     *
     * @param byteRepresentation the string representation
     */
    public DeploymentId(final byte[] byteRepresentation) {
        try {
            v1CompoundId = new V1CompoundId.Builder()
                    .with(byteRepresentation)
                    .only(APPLICATION)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidDeploymentIdException(ex);
        }
    }

    @Override
    public V1CompoundId getId() {
        return v1CompoundId;
    }

    /**
     * Gets the {@link UUID} associated with this DeploymentId
     *
     * @return the {@link UUID} for the deployment
     */
    public UUID getDeploymentUUID() {
        return v1CompoundId.getComponent(APPLICATION).getValue();
    }

    /**
     * Returns the {@link byte[]} representation of this {@link DeploymentId}
     * @return the value as bytes
     */
    public byte[] asBytes() {
        return bytes == null ? (bytes = v1CompoundId.asBytes(APPLICATION)) : bytes;
    }

    /**
     * Returns the string representation of this {@link DeploymentId}
     *
     * @return the string representation
     */
    public String asString() {
        return string == null ? (string = v1CompoundId.asEncodedString(APPLICATION)) : string;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentId)) return false;
        final DeploymentId deploymentId = (DeploymentId) o;
        return v1CompoundId.equals(deploymentId.v1CompoundId, APPLICATION);
    }

    @Override
    public int hashCode() {
        return hash == 0 ? (hash = v1CompoundId.hashCode(APPLICATION)) : hash;
    }

    @Override
    public String toString() {
        return asString();
    }

    /**
     * The Java standard valueOf method.
     *
     * @param value the value
     * @return the {@link DeploymentId}
     */
    public static DeploymentId valueOf(final String value) {
        return new DeploymentId(value);
    }

    /**
     * Generates a randomly assigned {@link DeploymentId}
     *
     * @return a randomly assigned globally unique {@link DeploymentId}
     */
    public static DeploymentId randomDeploymentId() {
        return new DeploymentId(randomUUID());
    }

    /**
     * Creates a new {@link DeploymentId} from the given unique deployment name.  The unique deployment name may be
     * any string uniquely representing the deployment (such as database primary key) or similar.
     *
     * @param uniqueDeploymentName the unique deployment name
     * @return the newly created {@link DeploymentId}
     */
    public static DeploymentId forUniqueName(final String uniqueDeploymentName) {
        final var bytes = uniqueDeploymentName.getBytes(V1CompoundId.CHARSET);
        final var deploymentUuid = nameUUIDFromBytes(bytes);
        return new DeploymentId(deploymentUuid);
    }

}
