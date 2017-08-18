package com.namazustudios.socialengine.rt.lua.converter;

import com.naef.jnlua.Converter;

/**
 * Created by patricktwohig on 8/18/17.
 */
public interface TypedConverter<JavaT> extends Converter {

    /**
     * Gets the converted type.
     *
     * @return the converted type
     */
    Class<JavaT> getConvertedType();

    /**
     * Tests if this {@link TypedConverter} can convert to the provided {@link Class<JavaT>} to a Lua object
     *
     * @param aClass the class
     * @return true if convertible, false otherwise
     */
    default boolean isConvertibleToLua(final Class<?> aClass) {
        return getConvertedType().equals(aClass);
    }

    /**
     * Tests if this {@link TypedConverter} can convert some Lua type to the provided Java {@link Class<JavaT>}
     *
     * @param aClass the class
     * @return true if convertible, false otherwise
     */
    default boolean isConvertibleFromLua(final Class<?> aClass) {
        return getConvertedType().isAssignableFrom(aClass);
    }

}
