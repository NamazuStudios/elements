package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import javolution.io.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.cluster.id.ResourceId.resourceIdFromString;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionParameter.Type.*;

public class UnixFSTransactionParameter {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionParameter.class);

    public static final Charset CHARSET = Charset.forName("UTF-8");

    private final Header header = new Header();

    private final ByteBuffer commandSlice;

    private UnixFSTransactionParameter(final UnixFSTransactionCommandHeader commandHeader, final int index) {

        final long parameterCount = commandHeader.parameterCount.get();

        if (index >= commandHeader.parameterCount.get()) {
            throw new UnixFSProgramCorruptionException("Parameter index out of bounds " + index + ">=" + parameterCount);
        } else if (index < 0) {
            throw new IllegalArgumentException("Invalid command parameter index: " + index);
        }

        ByteBuffer buffer;

        // Calculates a slice of the command buffer which is aligned to the beginning of the command where the header
        // starts. We slice the buffer so that all relative manipulation of the command can be accomplished using this
        // particular buffer.

        buffer = commandHeader.getByteBuffer().duplicate();
        buffer.position(commandHeader.getByteBufferPosition());
        commandSlice = buffer.slice();

        // The parameter's header position is between the end of the command header and the actual parameter data. It
        // is calculated as offset of the command added to the command header size, added to the index multiplied by
        // the size of the parameter header.
        final int position = buffer.position() + commandHeader.size() + (index * header.size());

        // Defensively we slice apart the buffer and make it the exact size and relative position of the command header
        // just in case the header has issues.

        buffer.position(position).limit(position + header.size());
        header.setByteBuffer(buffer.slice(), 0);

    }

    /**
     * Returns an instance of {@link UnixFSTransactionParameter} from the supplied {@link UnixFSTransactionCommand}
     * instance.
     *
     * @param unixFSTransactionCommand the {@link UnixFSTransactionCommand} which owns the parameter
     * @param index the parameter index.
     * @return an instance of {@link UnixFSTransactionParameter}
     */
    static UnixFSTransactionParameter fromCommand(final UnixFSTransactionCommand unixFSTransactionCommand,
                                                  final int index) {
        return new UnixFSTransactionParameter(unixFSTransactionCommand.header, index);
    }

    /**
     * Appends a FS Path parameter.
     *
     * @param commandHeader the {@link UnixFSTransactionCommandHeader}
     * @param parameterIndex the parameter index
     * @param stringValue the path to write
     */
    static void appendString(final UnixFSTransactionCommandHeader commandHeader,
                             final int parameterIndex,
                             final String stringValue) {

        final ByteBuffer commandByteBuffer = commandHeader.getByteBuffer();
        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(commandHeader, parameterIndex);

        final ByteBuffer encoded = CHARSET.encode(stringValue);
        encoded.rewind();

        final int length = encoded.remaining();
        final int position = commandByteBuffer.position() - commandHeader.getByteBufferPosition();

        param.header.type.set(STRING);
        param.header.length.set(length);
        param.header.position.set(position);
        commandByteBuffer.put(encoded);

    }

    /**
     * Appends a FS Path parameter.
     *
     * @param commandHeader the {@link UnixFSTransactionCommandHeader}
     * @param parameterIndex the parameter index
     * @param path the path to write
     */
    static void appendFSPath(final UnixFSTransactionCommandHeader commandHeader,
                             final int parameterIndex,
                             final java.nio.file.Path path) {

        final ByteBuffer commandByteBuffer = commandHeader.getByteBuffer();
        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(commandHeader, parameterIndex);

        final ByteBuffer encoded = CHARSET.encode(path.toString());
        encoded.rewind();

        final int length = encoded.remaining();
        final int position = commandByteBuffer.position() - commandHeader.getByteBufferPosition();

        param.header.type.set(FS_PATH);
        param.header.length.set(length);
        param.header.position.set(position);
        commandByteBuffer.put(encoded);

    }

    /**
     * Appends an RT Path parameter.
     *
     * @param commandHeader the {@link UnixFSTransactionCommandHeader}
     * @param parameterIndex the parameter index
     * @param path the path to write
     */
    static void appendRTPath(final UnixFSTransactionCommandHeader commandHeader,
                             final int parameterIndex,
                             final Path path) {

        final ByteBuffer commandByteBuffer = commandHeader.getByteBuffer();
        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(commandHeader, parameterIndex);

        final ByteBuffer encoded = CHARSET.encode(path.toNormalizedPathString());
        encoded.rewind();

        final int length = encoded.remaining();
        final int position = commandByteBuffer.position() - commandHeader.getByteBufferPosition();

        param.header.type.set(RT_PATH);
        param.header.length.set(length);
        param.header.position.set(position);
        commandByteBuffer.put(encoded);

    }

    /**
     * Appends a FS Path parameter.
     *
     * @param commandHeader the {@link UnixFSTransactionCommandHeader}
     * @param parameterIndex the parameter index
     * @param resourceId the {@link ResourceId} to write
     */
    static void appendResourceId(final UnixFSTransactionCommandHeader commandHeader,
                                 final int parameterIndex,
                                 final ResourceId resourceId) {

        final ByteBuffer commandByteBuffer = commandHeader.getByteBuffer();
        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(commandHeader, parameterIndex);

        final ByteBuffer encoded = CHARSET.encode(resourceId.asString());
        encoded.rewind();

        final int length = encoded.remaining();
        final int position = commandByteBuffer.position() - commandHeader.getByteBufferPosition();

        param.header.type.set(RESOURCE_ID);
        param.header.length.set(length);
        param.header.position.set(position);
        commandByteBuffer.put(encoded);

    }

    /**
     * Returns this parameter as an instance {@link java.nio.file.Path} or throws an instance of
     * {@link UnixFSProgramCorruptionException} if the type is not properly matched.
     *
     * @return a {@link java.nio.file.Path}
     */
    public java.nio.file.Path asFSPath() {
        try {
            return header.type.get().asFSPath(this);
        } catch (BufferUnderflowException ex) {
            throw new UnixFSProgramCorruptionException(ex);
        }
    }

    /**
     * Returns this parameter as an instance {@link Path} or throws an instance of
     * {@link UnixFSProgramCorruptionException} if the type is not properly matched.
     *
     * @return a {@link Path}
     */
    public Path asRTPath() {
        try {
            return header.type.get().asRTPath(this);
        } catch (BufferUnderflowException ex) {
            throw new UnixFSProgramCorruptionException(ex);
        }
    }

    /**
     * Returns this parameter as an instance {@link ResourceId} or throws an instance of
     * {@link UnixFSProgramCorruptionException} if the type is not properly matched.
     *
     * @return a {@link ResourceId}
     */
    public ResourceId asResourceId() {
        try {
            return header.type.get().asResourceId(this);
        } catch (BufferUnderflowException ex) {
            throw new UnixFSProgramCorruptionException(ex);
        }
    }

    /**
     * Returns this parameter instance as a {@link String} or throws and instance of
     * {@link UnixFSProgramCorruptionException} if the type is not properly matched.
     *
     * @return the parameter as a String
     */
    public String asString() {
        try {
            return header.type.get().asString(this);
        } catch (BufferUnderflowException ex) {
            throw new UnixFSProgramCorruptionException(ex);
        }
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();

        Type type;

        try {
            type = header.type.get();
        } catch (ArrayIndexOutOfBoundsException ex) {
            type = null;
        }

        final Function<Supplier<Object>, String> safeRender = suppler -> {
            try {
                return suppler.get().toString();
            } catch (Exception ex) {
                return "<corrupted>";
            }
        };

        if (type == null) {
            sb.append("<unknown>");
        } else {
            switch (type) {
                case FS_PATH:
                    sb.append(safeRender.apply(this::asFSPath));
                    break;
                case RT_PATH:
                    sb.append(safeRender.apply(this::asRTPath));
                    break;
                case RESOURCE_ID:
                    sb.append(safeRender.apply(this::asResourceId));
                    break;
                case STRING:
                    sb.append(safeRender.apply(this::asString));
                    break;
                default:
                    sb.append("<undefined>");
                    break;
            }
        }

        return sb.append(": ").append(type == null ? "<undefined>" : type).toString();

    }

    /**
     * The parameter header.
     */
    static class Header extends Struct {

        static int SIZE = new Header().size();

        private Header() {}

        /**
         * The parameter type.
         */
        private Enum8<Type> type = new Enum8<>(Type.values());

        /**
         * The parameter absolute position in the byte buffer representing the command parameter
         */
        private Unsigned32 position = new Unsigned32();

        /**
         * The parameter offset in the byte array following the header and parameter index.
         */
        private Unsigned32 length = new Unsigned32();

    }

    public enum Type {

        /**
         * Undefined
         */
        UNDEFINED,

        /**
         * Represents an arbitrary string.
         */
        STRING,

        /**
         * Represents a {@link ResourceId}
         */
        RESOURCE_ID {
            @Override
            protected ResourceId asResourceId(final UnixFSTransactionParameter param) {
                setPositionAndLimit(param);
                final var resourceIdString = asString(param);
                return resourceIdFromString(resourceIdString);
            }
        },

        /**
         * Represents a {@link java.nio.file.Path}.
         */
        FS_PATH {
            @Override
            protected java.nio.file.Path asFSPath(final UnixFSTransactionParameter param) {
                setPositionAndLimit(param);
                final var pathString = asString(param);
                return java.nio.file.Paths.get(pathString);
            }
        },

        /**
         * Represents a {@link Path}
         */
        RT_PATH {
            @Override
            protected Path asRTPath(final UnixFSTransactionParameter param) {
                setPositionAndLimit(param);
                final var pathString = asString(param);
                return Path.fromPathString(pathString);
            }
        };

        protected void setPositionAndLimit(final UnixFSTransactionParameter param) {
            final int position = (int) param.header.position.get();
            final int length = (int) param.header.length.get();
            param.commandSlice.position(position).limit(position + length);
        }

        protected String asString(final UnixFSTransactionParameter param) {
            setPositionAndLimit(param);
            return CHARSET.decode(param.commandSlice).toString();
        }

        protected java.nio.file.Path asFSPath(final UnixFSTransactionParameter unixFSTransactionParameter) {
            return badType(java.nio.file.Path.class);
        }

        protected Path asRTPath(final UnixFSTransactionParameter unixFSTransactionParameter) {
            return badType(Path.class);
        }

        protected ResourceId asResourceId(final UnixFSTransactionParameter unixFSTransactionParameter) {
            return badType(ResourceId.class);
        }

        protected  <T> T badType(final Class<?> cls) {
            throw new UnixFSProgramCorruptionException("Unexpected type \"" + cls.getName() + "\"");
        }

    }

}
