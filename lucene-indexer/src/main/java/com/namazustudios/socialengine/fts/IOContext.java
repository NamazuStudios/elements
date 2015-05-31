package com.namazustudios.socialengine.fts;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.Closeable;
import java.io.IOException;

/**
 * Manages the livecycle an index's IO object.  For example, this may manage an instance of
 * {@link IndexReader} or {@link IndexWriter}.
 *
 * Implementations of this may employ a caching or pooling strategy to avoid resource hogging.
 *
 * Instances of this object are meant to be short-lived and closed when no longer needed.  However
 * the implementation details may manage.
 *
 * Created by patricktwohig on 5/30/15.
 */
public interface IOContext<IOType> extends AutoCloseable, Closeable {

    /**
     * Returns the instance managed by this instance.  This must always return the same
     * instance and should return the instance with as little effort needed.
     *
     * @return an IndexReader used to read from the index.
     *
     * @throws IllegalStateException if the context has been closed
     */
    IOType instance();

    /**
     *
     * Used to indicate that this context is no longer needed.  This must, among other things,
     * abandon the instance returned by the {@link #instance()} method.  Once called
     * the state of this object is closed and further calls to this object must throw an instance
     * of {@link IllegalStateException}.
     *
     * @throws IllegalStateException if the context has been closed
     */
    void close() throws IOException;

    /**
     * Creates a new instance.
     * @param <IOType>
     */
    interface Provider<IOType> {

        /**
         * Gets a new instance of IOContext for the given type.
         *
         * @return
         */
        IOContext get();

    }

}
