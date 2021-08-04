package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.Private;

import java.io.PrintStream;

/**
 * Writes a {@link DocRoot} to persistent storage.
 */
@Private
public interface DocRootWriter extends AutoCloseable {

    /**
     * Gets the PrintStream used to write out the document. The returned {@link PrintStream} will always be the same
     * and closing this {@link DocWriter} will also close the stream.
     *
     * @return the {@link PrintStream} written to write the
     */
    PrintStream ps();

}
