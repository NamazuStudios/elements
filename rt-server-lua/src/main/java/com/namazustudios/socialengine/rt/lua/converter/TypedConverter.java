package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.jnlua.Converter;
import com.namazustudios.socialengine.jnlua.LuaState;

import static com.namazustudios.socialengine.jnlua.DefaultConverter.getInstance;

/**
 * Created by patricktwohig on 8/18/17.
 */
public interface TypedConverter<JavaT> extends Converter {

    @Override
    default int getTypeDistance(LuaState luaState, int index, Class<?> formalType) {
        return getInstance().getTypeDistance(luaState, index, formalType);
    }

    /**
     * Tests if this {@link TypedConverter} can convert some Lua type to the provided Java {@link Class<JavaT>}
     *
     * @param object the object to convert
     * @return true if convertible, false otherwise
     */
    default boolean isConvertibleToLua(final Object object) {
        return false;
    }

    /**
     * Tests if this {@link TypedConverter} can convert some Lua type to the provided Java {@link Class<JavaT>}
     *
     * @param formalType the class
     * @return true if convertible, false otherwise
     */
    default boolean isConvertibleFromLua(LuaState luaState, int index, Class<?> formalType) {
        return false;
    }

}
