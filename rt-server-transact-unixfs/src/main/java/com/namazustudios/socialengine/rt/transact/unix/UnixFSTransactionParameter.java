package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import javolution.io.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromByteBuffer;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionParameter.Type.*;

public class UnixFSTransactionParameter {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionParameter.class);

    private static final Charset CHARSET = Charset.forName("UTF-8");

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
        // starts. We slice the buffer so that all relative manipulation of the command can be accmplished using this
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
                             final com.namazustudios.socialengine.rt.Path path) {

        final ByteBuffer commandByteBuffer = commandHeader.getByteBuffer();
        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(commandHeader, parameterIndex);

        final ByteBuffer encoded = CHARSET.encode(path.toAbsolutePathString());
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
        final int position = commandByteBuffer.position() - commandHeader.getByteBufferPosition();

        param.header.type.set(RESOURCE_ID);
        param.header.length.set(ResourceId.getSizeInBytes());
        param.header.position.set(position);
        resourceId.toByteBuffer(commandByteBuffer);

    }

    /**
     * Returns this parameter as null or throws an instance of {@link UnixFSProgramCorruptionException} if the type is not
     * properly matched.
     *
     * @param <T> the type
     * @return null, always
     */
    public <T> T asNull() {
        try {
            return header.type.get().asNull(this);
        } catch (BufferUnderflowException ex) {
            throw new UnixFSProgramCorruptionException(ex);
        }
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
     * Returns this parameter as an instance {@link com.namazustudios.socialengine.rt.Path} or throws an instance of
     * {@link UnixFSProgramCorruptionException} if the type is not properly matched.
     *
     * @return a {@link com.namazustudios.socialengine.rt.Path}
     */
    public com.namazustudios.socialengine.rt.Path asRTPath() {
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
                case NULL:
                    sb.append("<null>");
                    break;
                case FS_PATH:
                    sb.append(safeRender.apply(this::asFSPath));
                    break;
                case RT_PATH:
                    sb.append(safeRender.apply(this::asRTPath));
                    break;
                case RESOURCE_ID:
                    sb.append(safeRender.apply(this::asResourceId));
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
         * Set to a null parameter.
         */
        NULL {
            @Override
            protected <T> T asNull(final UnixFSTransactionParameter param) {

                if (param.header.length.get() != 0) {
                    throw new UnixFSProgramCorruptionException("null parameter must be zero-length");
                }

                return null;

            }
        },

        /**
         * Represents a {@link ResourceId}
         */
        RESOURCE_ID {
            @Override
            protected ResourceId asResourceId(final UnixFSTransactionParameter param) {
                setPositionAndLimit(param);
                return resourceIdFromByteBuffer(param.commandSlice);
            }
        },

        /**
         * Represents a {@link java.nio.file.Path}.
         */
        FS_PATH {
            @Override
            protected java.nio.file.Path asFSPath(final UnixFSTransactionParameter param) {

                setPositionAndLimit(param);

                final CharBuffer charBuffer = CHARSET.decode(param.commandSlice);
                final String pathString = charBuffer.toString();

                return java.nio.file.Paths.get(pathString);

            }
        },

        /**
         * Represents a {@link com.namazustudios.socialengine.rt.Path}
         */
        RT_PATH {
            @Override
            protected com.namazustudios.socialengine.rt.Path asRTPath(final UnixFSTransactionParameter param) {

                setPositionAndLimit(param);

                final CharBuffer charBuffer = CHARSET.decode(param.commandSlice);
                final String pathString = charBuffer.toString();

                return com.namazustudios.socialengine.rt.Path.fromPathString(pathString);

            }
        };

        protected void setPositionAndLimit(final UnixFSTransactionParameter param) {
            final int position = (int) param.header.position.get();
            final int length = (int) param.header.length.get();
            param.commandSlice.position(position).limit(position + length);
        }

        protected <T> T asNull(UnixFSTransactionParameter unixFSTransactionParameter) {
            return badType(null);
        }

        protected java.nio.file.Path asFSPath(UnixFSTransactionParameter unixFSTransactionParameter) {
            return badType(java.nio.file.Path.class);
        }

        protected com.namazustudios.socialengine.rt.Path asRTPath(UnixFSTransactionParameter unixFSTransactionParameter) {
            return badType(com.namazustudios.socialengine.rt.Path.class);
        }

        protected ResourceId asResourceId(UnixFSTransactionParameter unixFSTransactionParameter) {
            return badType(ResourceId.class);
        }

        private <T> T badType(final Class<?> cls) {
            throw new UnixFSProgramCorruptionException("Unexpected type \"" + cls.getName() + "\"");
        }

    }

}
