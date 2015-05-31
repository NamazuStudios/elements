package com.namazustudios.socialengine.fts;

import java.io.Closeable;
import java.io.IOException;

/**
 * The abstract implementation of the {@link IOContext} class.
 *
 * Created by patricktwohig on 5/30/15.
 */
public abstract class AbstractIOContext<IOType> implements IOContext<IOType> {

    private final Class<?> type;
    private IOType instance;

    /**
     * Instantiates with an instance of the {@link IOType}.
     *
     * @param instance
     */
    public AbstractIOContext(final IOType instance) {
        type = instance.getClass();
        this.instance = instance;
    }

    @Override
    public final IOType instance() {

        if (instance == null) {
            throw new IllegalStateException("context closed for " + type);
        }

        return instance;

    }

    @Override
    public final void close() throws IOException {

        if (instance == null) {
            throw new IllegalStateException("context closed for " + type);
        }

        doClose();
        instance = null;

    }

    /**
     * Implements the implementation-specific close logic.  The {@link #close()} method of this object
     * keeps track of the state so this only needs to implement the actual closing operation.
     *
     * @throws IOException if the context failed to close
     */
    protected abstract void doClose() throws IOException;

}
