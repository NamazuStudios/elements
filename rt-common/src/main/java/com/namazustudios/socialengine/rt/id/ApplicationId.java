package com.namazustudios.socialengine.rt.id;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.V1CompoundId.Field.*;
import static java.util.UUID.nameUUIDFromBytes;
import static java.util.UUID.randomUUID;

public class ApplicationId implements Serializable {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient volatile byte[] bytes;

    private transient volatile String string;

    private ApplicationId() { v1CompoundId = null; }

    ApplicationId(final V1CompoundId v1CompoundId) {
        this.v1CompoundId = new V1CompoundId.Builder()
                .with(v1CompoundId)
                .without(INSTANCE, RESOURCE, TASK)
                .only(APPLICATION)
            .build();
    }

    /**
     * Creates a new unique {@link TaskId}.
     */
    public ApplicationId(final UUID applicationUuid) {
        v1CompoundId = new V1CompoundId.Builder()
                .with(APPLICATION, applicationUuid)
                .only(APPLICATION)
            .build();
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asString()}.
     *
     * @param stringRepresentation the string representation
     */
    public ApplicationId(final String stringRepresentation) {
        v1CompoundId = new V1CompoundId.Builder()
                .with(stringRepresentation)
                .only(APPLICATION)
            .build();
    }

    /**
     * Creates the {@link TaskId} from the provided string representation, as obtained from {@link #asBytes()}.
     *
     * @param byteRepresentation the string representation
     */
    public ApplicationId(final byte[] byteRepresentation) {
        v1CompoundId = new V1CompoundId.Builder()
                .with(byteRepresentation)
                .only(APPLICATION)
            .build();
    }

    /**
     * Gets the {@link UUID} associated with this ApplicationId
     *
     * @return the {@link UUID} for the application
     */
    public UUID getApplicationUUID() {
        return v1CompoundId.getComponent(APPLICATION).getValue();
    }

    /**
     * Returns the {@link byte[]} representation of this {@link TaskId}
     * @return
     */
    public byte[] asBytes() {
        return bytes == null ? (bytes = v1CompoundId.asBytes(APPLICATION)) : bytes;
    }

    /**
     * Returns the string representation of this {@link TaskId}
     *
     * @return the string representation
     */
    public String asString() {
        return string == null ? (string = v1CompoundId.asString(APPLICATION)) : string;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!ApplicationId.class.equals(o.getClass())) return false;
        final ApplicationId applicationId = (ApplicationId) o;
        return v1CompoundId.equals(applicationId.v1CompoundId, APPLICATION);
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
     * Generates a randomly assigned {@link ApplicationId}
     *
     * @return a randomly assigned globally unique {@link ApplicationId}
     */
    public static ApplicationId randomApplicationId() {
        return new ApplicationId(randomUUID());
    }

    /**
     * Creates a new {@link ApplicationId} from the given unique application name.  The unique application name may be
     * any string uniquely representing the application (such as database primary key) or similar.
     *
     * @param uniqueApplicationName the unique application name
     * @return the newly created {@link ApplicationId}
     */
    public static ApplicationId forUniqueName(final String uniqueApplicationName) {
        final byte[] bytes = uniqueApplicationName.getBytes(CHARSET);
        final UUID applicationUuid = nameUUIDFromBytes(bytes);
        return new ApplicationId(applicationUuid);
    }

}
