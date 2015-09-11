package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.exception.NotFoundException;

/**
 * Various scripts in the server may need to access type metadata.  This allows for a layer
 * of abstraction, if desired.
 *
 * Created by patricktwohig on 8/26/15.
 */
public interface TypeRegistry {

    /**
     * Gets the request type with the given name.
     *
     * @param name the name of the event
     * @return the type
     *
     * @throws {@link NotFoundException} if the type cannot be found
     */
    Class<?> getRequestPayloadTypeNamed(final String name);

}
