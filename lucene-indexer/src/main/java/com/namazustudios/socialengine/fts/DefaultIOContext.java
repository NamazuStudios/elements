package com.namazustudios.socialengine.fts;

import java.io.Closeable;
import java.io.IOException;

/**
 * A simple basic implementation of {@link IOContext}.
 *
 * Created by patricktwohig on 5/30/15.
 */
public class DefaultIOContext<IOType extends Closeable> extends AbstractIOContext<IOType> {

    /**
     * Acepts an instance of {@link IOType} to wrap.
     *
     * @param instance the instance ot wrap
     */
    public DefaultIOContext(final IOType instance) {
        super(instance);
    }

    @Override
    protected void doClose() throws IOException {
        instance().close();
    }

}
