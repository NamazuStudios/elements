package com.namazustudios.socialengine.rt;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 8/22/17.
 */
public interface Bootstrapper {

    /**
     * Gets a {@link Map<Path, Supplier<InputStream>>} instance which will provide
     * a comprehensive set of resources which should go into a freshly created set
     * of code.
     *
     * @return a {@link Map<Path, Supplier<InputStream>>} instance
     */
    Map<Path, Supplier<InputStream>> getBootstrapResources();

}
