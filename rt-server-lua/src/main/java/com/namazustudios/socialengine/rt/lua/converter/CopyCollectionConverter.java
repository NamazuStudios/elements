package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.jnlua.LuaState;

import com.namazustudios.socialengine.jnlua.LuaType;
import com.namazustudios.socialengine.jnlua.LuaValueProxy;
import com.namazustudios.socialengine.rt.lua.Constants;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import java.util.*;

import static com.namazustudios.socialengine.jnlua.DefaultConverter.*;
import static com.namazustudios.socialengine.jnlua.LuaType.TABLE;
import static com.namazustudios.socialengine.rt.lua.Constants.*;
import static com.namazustudios.socialengine.rt.manifest.model.Type.ARRAY;
import static com.namazustudios.socialengine.rt.manifest.model.Type.OBJECT;

public class CopyCollectionConverter<ObjectT> implements TypedConverter<ObjectT> {

    @Override
    public <T> T convertLuaValue(final LuaState luaState, final int index, final Class<T> formalType) {

        final boolean isArray = isArray(luaState, index);

        if (isArray && (formalType.isAssignableFrom(Iterable.class) || Object.class.equals(formalType))) {
            final List<?> proxyList = getInstance().convertLuaValue(luaState, index, List.class);
            return (T) new ArrayList<Object>(proxyList);
        } else if (formalType.isAssignableFrom(Map.class) || Object.class.equals(formalType)) {
            final Map<?, ?> proxyMap = getInstance().convertLuaValue(luaState, index, Map.class);
            return (T) new LinkedHashMap<Object, Object>(proxyMap);
        } else {
            final LuaType luaType = luaState.type(index);
            throw new IllegalArgumentException("Unexpected " + luaType + " on the Lua stack requested conversion.");
        }

    }

    private boolean isArray(final LuaState luaState, final int index) {

        final int top = luaState.getTop();

        try {

            luaState.getMetatable(index);
            luaState.getField(-1, MANIFEST_TYPE_METAFIELD);

            final String value = luaState.toString(-1);

            if (ARRAY.value.equals(value)) {
                return true;
            } else if (OBJECT.value.equals(value)) {
                return false;
            }

        } finally {
            luaState.setTop(top);
        }

        try {

            // Inspect the table to see

            luaState.pushValue(index);
            luaState.pushNil();

            int count = 0;

            for (int expected = 1; luaState.next(-2); ++expected, ++count) {

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

            return count > 0;

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

            luaState.newTable();
            luaState.pushString(OBJECT.value);
            luaState.setField(-2, MANIFEST_TYPE_METAFIELD);
            luaState.setMetatable(-2);

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

            luaState.newTable();
            luaState.pushString(ARRAY.value);
            luaState.setField(-2, MANIFEST_TYPE_METAFIELD);
            luaState.setMetatable(-2);

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
        return luaState.type(index) == TABLE && (
            formalType.isAssignableFrom(Map.class) ||
            formalType.isAssignableFrom(Iterable.class) ||
            Object.class.equals(formalType)
        );
    }

}
