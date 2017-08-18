package com.namazustudios.socialengine.rt.lua.converter;

import com.naef.jnlua.DefaultConverter;
import com.naef.jnlua.LuaState;

import java.util.Map;

/**
 * Created by patricktwohig on 8/18/17.
 */
public abstract class AbstractMapConverter<JavaT> implements TypedConverter<JavaT> {

    private final DefaultConverter defaultConverter = DefaultConverter.getInstance();

    @Override
    public int getTypeDistance(LuaState luaState, int index, Class<?> formalType) {
        return defaultConverter.getTypeDistance(luaState, index, formalType);
    }

    @Override
    public <T> T convertLuaValue(LuaState luaState, int index, Class<T> formalType) {
        final Map<?, ?> map = defaultConverter.convertLuaValue(luaState, index, Map.class);
        final JavaT object = convertLua2Java(map);
        return formalType.cast(object);
    }

    @Override
    public void convertJavaObject(LuaState luaState, Object object) {
        final JavaT javaTObject = getConvertedType().cast(object);
        final Map<?, ?> map = convertJava2Lua(javaTObject);
        defaultConverter.convertJavaObject(luaState, map);
    }

    protected Map<?,?> convertJava2Lua(JavaT object) {
        throw new UnsupportedOperationException("Conversion Java -> Lua not supported for " + getConvertedType());
    }

    protected JavaT convertLua2Java(Map<?,?> map) {
        throw new UnsupportedOperationException("Conversion Lua -> Java not supported for " + getConvertedType());
    }

}
