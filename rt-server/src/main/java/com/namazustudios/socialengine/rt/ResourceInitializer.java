package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.internal.InternalResource;

/**
 * Returns an instance of a {@link Resource} fully initalized.  This is used in cases
 * where initialization may be needed for the sake of atomicity.ß
 */
public interface ResourceInitializer<ResourceT extends Resource> {

    /**
     * Returns a fully initialized instance of the {@link InternalResource}
     *
     * @return the resource initializer.
     */
    ResourceT init();

}
