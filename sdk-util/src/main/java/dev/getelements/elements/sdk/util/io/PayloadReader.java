package dev.getelements.elements.sdk.util.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Handles the actual details of writing to the {@link OutputStream}.
 */
public interface PayloadReader {

    /**
     * Reads the object to another type.
     *
     * @param to the type to read
     * @param from the type from which to read
     * @param <T> the type
     * @return an instance of the requested type, read from the supplied object.
     */
    <T> T convert(final Class<T> to, final Object from);

    /**
     * Reads the actual response object to the {@link InputStream}.
     *
     * @param payloadType the the response object
     * @param stream the output stream
     * @throws IOException
     */
    <T> T read(Class<T> payloadType, InputStream stream) throws IOException;

    /**
     * Reads a payload from the supplied byte array.  The default implementation of this method uses a temporary
     * {@link ByteArrayInputStream} to accomplish the deserialization.
     *
     * @param payloadType the type of the payload
     * @param toRead the byte array to read
     * @param <T> the type of the payload
     * @return the deserialized payload
     * @throws IOException
     */
    default <T> T read(final Class<T> payloadType, final byte[] toRead) throws IOException {
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(toRead)) {
            return read(payloadType, bis);
        }
    }

    /**
     * Reads a payload from the supplied {@link ByteBuffer}.  The default implementation of this method delegates
     * to {@link #read(Class, byte[])} using the buffer's backing array.
     *
     * @param payloadType the type of the payload
     * @param bytes the {@link ByteBuffer} to read
     * @param <T> the type of the payload
     * @return the deserialized payload
     * @throws IOException if there is a problem reading the bytes
     */
    default <T> T read(final Class<T> payloadType, final ByteBuffer bytes) throws IOException {
        final var array = new byte[bytes.remaining()];
        bytes.get(array);
        return read(payloadType, array);
    }

}
