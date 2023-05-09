package dev.getelements.elements.rt.lua.converter;

import dev.getelements.elements.jnlua.Converter;
import dev.getelements.elements.jnlua.LuaState;

import static dev.getelements.elements.jnlua.DefaultConverter.getInstance;

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
    default boolean isConvertibleFromLua(final LuaState luaState, int index, Class<?> formalType) {
        return false;
    }

}
