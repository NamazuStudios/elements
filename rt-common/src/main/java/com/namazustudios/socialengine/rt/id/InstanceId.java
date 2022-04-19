package com.namazustudios.socialengine.rt.id;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.InvalidInstanceIdException;
import com.namazustudios.socialengine.rt.util.TemporaryFiles;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.id.V1CompoundId.Builder;
import static com.namazustudios.socialengine.rt.id.V1CompoundId.Field.*;
import static java.nio.file.Files.move;
import static java.util.UUID.nameUUIDFromBytes;
import static java.util.UUID.randomUUID;

public class InstanceId implements Serializable, HasCompoundId<V1CompoundId>  {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(InstanceId.class);

    final V1CompoundId v1CompoundId;

    private transient volatile int hash;

    private transient byte[] bytes;

    private transient volatile String string;

    private InstanceId() {
        try {
            v1CompoundId = new Builder()
                    .with(INSTANCE, randomUUID())
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidInstanceIdException(ex);
        }
    }

    public InstanceId(final String stringRepresentation) {
        try {
            v1CompoundId = new Builder()
                    .with(stringRepresentation)
                    .only(INSTANCE)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidInstanceIdException(ex);
        }
    }

    public InstanceId(final byte[] byteRepresentation) {
        try {
            v1CompoundId = new Builder()
                    .with(byteRepresentation)
                    .only(INSTANCE)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidInstanceIdException(ex);
        }
    }

    public InstanceId(final UUID instanceUuid) {
        try {
            v1CompoundId = new Builder()
                    .with(INSTANCE, instanceUuid)
                    .only(INSTANCE)
                .build();
        } catch (IllegalArgumentException ex) {
            throw new InvalidInstanceIdException(ex);
        }
    }

    InstanceId(final V1CompoundId v1CompoundId) {
        try {
            this.v1CompoundId = new Builder()
                    .with(v1CompoundId)
                    .without(APPLICATION, RESOURCE, TASK)
                    .only(INSTANCE)
                .build();

        } catch (IllegalArgumentException ex) {
            throw new InvalidInstanceIdException(ex);
        }
    }

    /**
     * Gets a {@link UUID} representing the instance id.
     *
     * @return the {@link UUID} of this {@link InstanceId}
     */
    public UUID getUuid() {
        return v1CompoundId.getComponent(INSTANCE).getValue();
    }

    /**
     * Returns the compound Id string representation of this {@link NodeId}
     *
     * @return the string representation
     */
    public String asString() {
        return string == null ? (string = v1CompoundId.asEncodedString(INSTANCE)) : string;
    }

    public byte[] asBytes() {
        return (bytes == null ? (bytes = v1CompoundId.asBytes(INSTANCE)) : bytes).clone();
    }

    @Override
    public V1CompoundId getId() {
        return v1CompoundId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!InstanceId.class.equals(o.getClass())) return false;
        final InstanceId other = (InstanceId)o;
        return v1CompoundId.equals(other.v1CompoundId, INSTANCE);
    }

    @Override
    public int hashCode() {
        return hash == 0 ? (hash = v1CompoundId.hashCode(INSTANCE)) : hash;
    }

    @Override
    public String toString() {
        return asString();
    }

    /**
     * The Java standard valueOf method.
     *
     * @param value the value
     * @return the {@link InstanceId}
     */
    public static InstanceId valueOf(final String value) {
        return new InstanceId(value);
    }

    /**
     * Generates a randomly-assigned unique {@link InstanceId}.
     *
     * @return the generated {@link InstanceId}
     */
    public static InstanceId randomInstanceId() {
        // Here to protect against implicit bindings in DI Containers.
        return new InstanceId();
    }

    /**
     * Creates a new {@link InstanceId} from the given unique instance name.  The unique instance name may be
     * any string uniquely representing the instance (such as database primary key, DNS hostname) or similar.
     *
     * @param uniqueInstanceName the unique instance name
     * @return the newly created {@link InstanceId}
     */
    public static InstanceId forUniqueName(final String uniqueInstanceName) {
        final var bytes = uniqueInstanceName.getBytes(V1CompoundId.CHARSET);
        final var applicationUuid = nameUUIDFromBytes(bytes);
        return new InstanceId(applicationUuid);
    }

    /**
     * Creates a file on disk at the supplied path to hold an {@link InstanceId}. A file already exists at at that
     * location and it is a valid instance ID, it will simply return that value read from disk.
     *
     * @param path the path to the instance ID file
     * @return the {@link InstanceId} read or generated
     */
    public static InstanceId loadOrGenerate(final String path) {
        final var file = new File(path);
        return loadOrGenerate(file);
    }

    /**
     * Creates a file on disk at the supplied path to hold an {@link InstanceId}. A file already exists at at that
     * location and it is a valid instance ID, it will simply return that value read from disk.
     *
     * @param file the path to the instance ID file
     * @return the {@link InstanceId} read or generated
     */
    public static InstanceId loadOrGenerate(final File file) {
        try (final var fis = new FileInputStream(file);
             final var reader = new BufferedReader(new InputStreamReader(fis))) {
            return read(reader);
        } catch (FileNotFoundException | InvalidInstanceIdException ex) {
            return generateAndWrite(file);
        } catch (IOException ex) {
            throw new InternalException("Unable to read InstanceId from disk.", ex);
        }
    }

    private static InstanceId read(final File file) {
        try (final var fis = new FileInputStream(file);
             final var reader = new BufferedReader(new InputStreamReader(fis))) {
            return read(reader);
        } catch (IOException ex) {
            throw new InternalException("Unable to read InstanceId from disk.", ex);
        }
    }

    private static InstanceId read(final BufferedReader reader) throws IOException {
        final String stringRepresentation = reader.readLine();
        return new InstanceId(stringRepresentation);
    }

    private static InstanceId generateAndWrite(final File file) {

        final var parent = file.getParentFile();

        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new InternalException("Could not make directories.");
        }

        final File temp;

        try {
            temp = temporaryFiles.createTempFile("instance-id", ".txt").toFile();
            temp.deleteOnExit();
        } catch (UncheckedIOException ex) {
            throw new InternalException(ex.getCause());
        }

        final InstanceId instanceId = randomInstanceId();

        try (final OutputStream os = new FileOutputStream(temp);
             final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
            writer.write(instanceId.asString());
            writer.write('\n');
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

        try {
            move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (FileNotFoundException e) {
            return read(file);
        } catch (IOException ex) {
            throw new InternalException("Unable to read InstanceId from disk.", ex);
        }

        return instanceId;

    }

}
