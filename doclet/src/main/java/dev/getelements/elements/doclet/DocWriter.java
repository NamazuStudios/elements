package dev.getelements.elements.doclet;

import dev.getelements.elements.rt.annotation.Private;

import java.io.Closeable;
import java.io.IOException;

/**
 * Opens a {@link DocWriter}.
 */
@Private
public interface DocWriter extends Closeable {

    /**
     * Opens a {@link DocRootWriter} used to write the contents of the supplied {@link DocRoot}. Once finished, the
     * returned {@link DocRootWriter} must be closed by a call to {@link AutoCloseable#close()}.
     *
     * @param docRoot the {@link DocRoot}
     * @return the {@link DocRootWriter}
     * @throws IOException if there is a problem opening the {@link DocRootWriter}.
     */
    DocRootWriter open(final DocRoot docRoot) throws IOException;

    /**
     * Resets the writer. This clears the state of the the writer and makes it suitable for a fresh write.
     *
     * @throws IOException
     */
    default void reset() throws IOException {};

    @Override
    default void close() throws IOException {}

}
