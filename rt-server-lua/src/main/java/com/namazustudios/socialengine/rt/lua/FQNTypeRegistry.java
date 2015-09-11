package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.exception.InternalException;

/**
 * Created by patricktwohig on 9/10/15.
 */
public class FQNTypeRegistry implements TypeRegistry {

    @Override
    public Class<?> getRequestPayloadTypeNamed(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }
    }

}
