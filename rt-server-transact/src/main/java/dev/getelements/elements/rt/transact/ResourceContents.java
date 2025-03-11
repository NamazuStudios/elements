package dev.getelements.elements.rt.transact;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public interface ResourceContents {

    /**
     * Reads from the channel.
     *
     * @return the {@link ReadableByteChannel} to read the contents
     * @throws IOException if there was an error reading the underlying contents
     */
    ReadableByteChannel read() throws IOException;

    /**
     * Writes to the channel, throwing an exception if not supported.
     *
     * @return the {@link ReadableByteChannel} to read the contents
     * @throws IOException if there was an error writing the underlying contents
     */
    default WritableByteChannel write(final String transactionId) throws IOException {
        return tryWrite(transactionId).orElseThrow(() -> new UnsupportedOperationException("Immutable entry."));
    }

    /**
     * Writes to the channel. Returning an empty Optional if not supported.
     *
     * @return the {@link ReadableByteChannel} to read the contents
     * @throws IOException if there was an error writing the underlying contents
     */
    default Optional<WritableByteChannel> tryWrite(final String transactionId) throws IOException {
        return Optional.empty();
    }

}
