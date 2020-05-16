package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.ResourceId;
import javolution.io.Struct;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromByteBuffer;

public class UnixFSTransactionParameter {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final Header header = new Header();

    private final ByteBuffer byteBuffer;

    private UnixFSTransactionParameter(final UnixFSTransactionCommand command, final int index) {

        final long parameterCount = command.header.parameterCount.get();

        if (index >= command.header.parameterCount.get()) {
            throw new UnixFSProgramCorruptionException("Parameter index out of bounds " + index + ">=" + parameterCount);
        }

        this.byteBuffer = command.byteBuffer;

        final int position = calculateHeaderPositionRelative(command, index);
        header.setByteBuffer(byteBuffer, position);

    }

    private int calculateHeaderPositionRelative(final UnixFSTransactionCommand command, final int index) {
        return command.header.size() + (header.size() * index);
    }

    /**
     * Returns an instance of {@link UnixFSTransactionParameter} from the supplied {@link UnixFSTransactionCommand}
     * instance.
     *
     * @param index the parameter index.
     * @param unixFSTransactionCommand the {@link UnixFSTransactionCommand} which owns the parameter
     * @return an instance of {@link UnixFSTransactionParameter}
     */
    static UnixFSTransactionParameter fromCommand(final int index,
                                                  final UnixFSTransactionCommand unixFSTransactionCommand) {
        return new UnixFSTransactionParameter(unixFSTransactionCommand, index);
    }

    /**
     * Appends a FS Path parameter.
     *
     * @param command the command owning the parameter
     * @param parameterIndex the parameter index
     * @param path the path to write
     */
    static void appendFSPath(final UnixFSTransactionCommand command,
                             final int parameterIndex,
                             final java.nio.file.Path path) {

        final ByteBuffer encoded = CHARSET.encode(path.toString());
        encoded.rewind();

        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(command, parameterIndex);
        param.header.length.set(encoded.remaining());
        param.header.position.set(command.byteBuffer.position() - command.header.getByteBufferPosition());

        command.byteBuffer.put(encoded);

    }

    /**
     * Appends an RT Path parameter.
     *
     * @param command the command owning the parameter
     * @param parameterIndex the parameter index
     * @param path the path to write
     */
    static void appendRTPath(final UnixFSTransactionCommand command,
                             final int parameterIndex,
                             final com.namazustudios.socialengine.rt.Path path) {

        final ByteBuffer encoded = CHARSET.encode(path.toAbsolutePathString());
        encoded.rewind();

        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(command, parameterIndex);
        param.header.length.set(encoded.remaining());
        param.header.position.set(command.byteBuffer.position() - command.header.getByteBufferPosition());

        command.byteBuffer.put(encoded);

    }

    /**
     * Appends a FS Path parameter.
     *
     * @param command the command owning the parameter
     * @param parameterIndex the parameter index
     * @param resourceId the {@link ResourceId} to write
     */
    static void appendResourceId(final UnixFSTransactionCommand command,
                                 final int parameterIndex,
                                 final ResourceId resourceId) {
        final UnixFSTransactionParameter param = new UnixFSTransactionParameter(command, parameterIndex);
        param.header.length.set(ResourceId.getSizeInBytes());
        param.header.position.set(command.byteBuffer.position() - command.header.getByteBufferPosition());
        resourceId.toByteBuffer(command.byteBuffer);
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
            protected <T> T asNull(final UnixFSTransactionParameter unixFSTransactionParameter) {

                if (unixFSTransactionParameter.header.length.get() != 0) {
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
            protected ResourceId asResourceId(final UnixFSTransactionParameter unixFSTransactionParameter) {
                setPositionAndLimit(unixFSTransactionParameter);
                return resourceIdFromByteBuffer(unixFSTransactionParameter.byteBuffer);
            }
        },

        /**
         * Represents a {@link java.nio.file.Path}.
         */
        FS_PATH {
            @Override
            protected java.nio.file.Path asFSPath(final UnixFSTransactionParameter unixFSTransactionParameter) {

                setPositionAndLimit(unixFSTransactionParameter);

                final CharBuffer charBuffer = CHARSET.decode(unixFSTransactionParameter.byteBuffer);
                final String pathString = charBuffer.toString();

                return java.nio.file.Paths.get(pathString);

            }
        },

        /**
         * Represents a {@link com.namazustudios.socialengine.rt.Path}
         */
        RT_PATH {
            @Override
            protected com.namazustudios.socialengine.rt.Path asRTPath(
                    final UnixFSTransactionParameter unixFSTransactionParameter) {

                setPositionAndLimit(unixFSTransactionParameter);

                final CharBuffer charBuffer = CHARSET.decode(unixFSTransactionParameter.byteBuffer);
                final String pathString = charBuffer.toString();

                return com.namazustudios.socialengine.rt.Path.fromPathString(pathString);

            }
        };

        protected void setPositionAndLimit(final UnixFSTransactionParameter unixFSTransactionParameter) {

            final int position = (int) unixFSTransactionParameter.header.position.get();
            final int limit = (int) unixFSTransactionParameter.header.length.get();

            unixFSTransactionParameter.byteBuffer
                    .position(position)
                    .limit(position + limit);

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
