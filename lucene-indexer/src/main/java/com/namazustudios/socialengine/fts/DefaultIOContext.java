package com.namazustudios.socialengine.fts;

import java.io.Closeable;
import java.io.IOException;

/**
 * A simple basic implementation of {@link IOContext}.
 *
 * Created by patricktwohig on 5/30/15.
 */
public class DefaultIOContext<IOType extends Closeable> implements IOContext<IOType> {

    private IOType instance;
    private final Class<?> type;

    /**
     * Acepts an instance of {@link IOType} to wrap.
     *
     * @param instance the instance ot wrap
     */
    public DefaultIOContext(final IOType instance) {
        this.instance = instance;
        type = instance.getClass();
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

        instance.close();
        instance = null;

    }

}
