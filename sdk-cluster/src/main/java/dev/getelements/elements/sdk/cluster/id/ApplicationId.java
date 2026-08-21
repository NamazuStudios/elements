package dev.getelements.elements.sdk.cluster.id;

import java.util.UUID;

import static java.util.UUID.nameUUIDFromBytes;

/**
 * Uniquely identifies an application.
 *
 * @deprecated Replaced with {@link DeploymentId}.
 */
@Deprecated
public class ApplicationId extends DeploymentId  {

    ApplicationId(final V1CompoundId v1CompoundId) {
        super(v1CompoundId);
    }

    /**
     * Creates a new unique {@link TaskId}.
     */
    public ApplicationId(final UUID applicationUuid) {
        super(applicationUuid);
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asString()}.
     *
     * @param stringRepresentation the string representation
     */
    public ApplicationId(final String stringRepresentation) {
        super(stringRepresentation);
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asBytes()}.
     *
     * @param byteRepresentation the string representation
     */
    public ApplicationId(final byte[] byteRepresentation) {
        super(byteRepresentation);
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
        final var applicationUuid = nameUUIDFromBytes(bytes);
        return new ApplicationId(applicationUuid);
    }

}
