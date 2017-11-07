package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.jnlua.DefaultConverter;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.jnlua.LuaValueProxy;

import java.util.Map;

import static com.namazustudios.socialengine.jnlua.DefaultConverter.*;

public class ProxyConverter<ObjectT> implements TypedConverter<ObjectT> {

    @Override
    public int getTypeDistance(LuaState luaState, int index, Class<?> formalType) {
        return getInstance().getTypeDistance(luaState, index, formalType);
    }

    @Override
    public <T> T convertLuaValue(LuaState luaState, int index, Class<T> formalType) {
        return getInstance().convertLuaValue(luaState, index, formalType);
    }

    @Override
    public void convertJavaObject(LuaState luaState, Object object) {

        final Map<?, ?> asMap = (Map<?,?>)object;
        final LuaValueProxy asLuaValueProxy = (LuaValueProxy) object;

        if (asLuaValueProxy.getLuaState().equals(luaState)) {
            getInstance().convertJavaObject(luaState, object);
        } else {

            luaState.newTable();

            asMap.forEach((k, v) -> {
                luaState.pushJavaObject(k);
                luaState.pushJavaObject(v);
                luaState.setTable(-3);
            });

        }

    }

    @Override
    public boolean isConvertibleFromLua(Class<?> aClass) {
        return LuaValueProxy.class.isAssignableFrom(aClass) && Map.class.isAssignableFrom(aClass);
    }

}
