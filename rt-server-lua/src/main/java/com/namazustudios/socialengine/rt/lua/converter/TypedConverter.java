package com.namazustudios.socialengine.rt.lua.converter;

import com.naef.jnlua.Converter;
import com.naef.jnlua.LuaState;

/**
 * Created by patricktwohig on 8/18/17.
 */
public interface TypedConverter<JavaT> extends Converter {

    /**
     * Tests if this {@link TypedConverter} can convert some Lua type to the provided Java {@link Class<JavaT>}
     *
     * @param aClass the class
     * @return true if convertible, false otherwise
     */
    boolean isConvertibleFromLua(final Class<?> aClass);
}
