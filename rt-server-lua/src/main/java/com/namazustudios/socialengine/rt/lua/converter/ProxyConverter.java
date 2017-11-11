package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.jnlua.LuaState;

import com.namazustudios.socialengine.jnlua.LuaType;
import com.namazustudios.socialengine.jnlua.LuaValueProxy;

import java.util.*;

import static com.namazustudios.socialengine.jnlua.DefaultConverter.*;
import static com.namazustudios.socialengine.jnlua.LuaType.TABLE;

public class ProxyConverter<ObjectT> implements TypedConverter<ObjectT> {

    @Override
    public <T> T convertLuaValue(final LuaState luaState, final int index, final Class<T> formalType) {
        if (Iterable.class.isAssignableFrom(formalType) && isLuaSequence(luaState, index)) {
            final List<?> proxyList = getInstance().convertLuaValue(luaState, index, List.class);
            return (T) new ArrayList<Object>(proxyList);
        } else if (formalType.isAssignableFrom(Map.class)) {
            final Map<?, ?> proxyMap = getInstance().convertLuaValue(luaState, index, Map.class);
            return (T) new LinkedHashMap<Object, Object>(proxyMap);
        } else {
            final LuaType luaType = luaState.type(index);
            throw new IllegalArgumentException("Unexpected " + luaType + " on the Lua stack requested conversion.");
        }
    }

    private boolean isLuaSequence(final LuaState luaState, final int index) {

        final int top = luaState.getTop();

        try {

            luaState.pushValue(index);
            luaState.pushNil();

            for (int expected = 1; luaState.next(-2); ++expected) {

                luaState.pushValue(-2);

                if (luaState.type(-1) != LuaType.NUMBER) {
                    return false;
                }

                final double actual = luaState.toNumber(-1);

                if (actual != expected) {
                    return false;
                }

                luaState.pop(2);

            }

            return true;

        } finally {
            luaState.setTop(top);
        }

    }

    @Override
    public void convertJavaObject(LuaState luaState, Object object) {
        if (object instanceof LuaValueProxy && luaState.equals(((LuaValueProxy) object).getLuaState())) {
            getInstance().convertJavaObject(luaState, object);
        } else if (object instanceof Map) {

            final Map<?, ?> map = (Map<?,?>)object;

            luaState.newTable();

            map.forEach((k, v) -> {
                luaState.pushJavaObject(k);
                luaState.pushJavaObject(v);
                luaState.setTable(-3);
            });

        } else if (object instanceof Iterable) {

            int index = 0;
            final Iterable<?> list = (Iterable<?>)object;
            final Iterator<?> listIterator = list.iterator();

            luaState.newTable();

            while (listIterator.hasNext()) {
                final Object element = listIterator.next();
                luaState.pushJavaObject(element);
                luaState.rawSet(-2, ++index);
            }

        } else {
            throw new IllegalArgumentException("Unexpected object " + object + " attempted to convert.");
        }
    }

    @Override
    public boolean isConvertibleToLua(Object object) {
        return (object instanceof Iterable) || (object instanceof Map);
    }

    @Override
    public boolean isConvertibleFromLua(final LuaState luaState, final int index, final Class<?> formalType) {
        return luaState.type(index) == TABLE && (formalType.isAssignableFrom(Map.class) || formalType.isAssignableFrom(List.class));
    }

}
