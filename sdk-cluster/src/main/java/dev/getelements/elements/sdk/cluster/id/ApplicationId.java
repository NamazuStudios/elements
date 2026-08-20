package dev.getelements.elements.sdk.cluster.id;

import java.util.UUID;

/**
 * Uniquely identifies an application.
 *
 * @deprecated use {@link DeploymentId}
 */
@Deprecated
public class ApplicationId extends DeploymentId {

    ApplicationId(final V1CompoundId v1CompoundId) {
        super(v1CompoundId);
    }

    /**
     * Creates a new unique {@link ApplicationId}.
     */
    public ApplicationId(final UUID applicationUuid) {
        super(applicationUuid);
    }

    /**
     * Creates the {@link ApplicationId} from the provided string representation, as obtained from {@link #asString()}.
     *
     * @param stringRepresentation the string representation
     */
    public ApplicationId(final String stringRepresentation) {
        super(stringRepresentation);
    }

    /**
     * Creates the {@link ApplicationId} from the provided string representation, as obtained from {@link #asBytes()}.
     *
     * @param byteRepresentation the string representation
     */
    public ApplicationId(final byte[] byteRepresentation) {
        super(byteRepresentation);
    }

    /**
     * Gets the {@link UUID} associated with this ApplicationId
     *
     * @return the {@link UUID} for the application
     * @deprecated use {@link #getDeploymentUUID()}
     */
    @Deprecated
    public UUID getApplicationUUID() {
        return getDeploymentUUID();
    }

    /**
     * The Java standard valueOf method.
     *
     * @param value the value
     * @return the {@link ApplicationId}
     */
    public static ApplicationId valueOf(final String value) {
        return new ApplicationId(value);
    }

    /**
     * Generates a randomly assigned {@link ApplicationId}
     *
     * @return a randomly assigned globally unique {@link ApplicationId}
     */
    public static ApplicationId randomApplicationId() {
        return new ApplicationId(UUID.randomUUID());
    }

    /**
     * Creates a new {@link ApplicationId} from the given unique application name.  The unique application name may be
     * any string uniquely representing the application (such as database primary key) or similar.
     *
     * @param uniqueApplicationName the unique application name
     * @return the newly created {@link ApplicationId}
     */
    public static ApplicationId forUniqueName(final String uniqueApplicationName) {
        final var bytes = uniqueApplicationName.getBytes(V1CompoundId.CHARSET);
        final var applicationUuid = UUID.nameUUIDFromBytes(bytes);
        return new ApplicationId(applicationUuid);
    }

}
