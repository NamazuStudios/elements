package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.jnlua.LuaState;

import com.namazustudios.socialengine.jnlua.LuaType;
import com.namazustudios.socialengine.jnlua.LuaValueProxy;
import com.namazustudios.socialengine.rt.lua.Constants;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import java.lang.reflect.Array;
import java.util.*;

import static com.namazustudios.socialengine.jnlua.DefaultConverter.*;
import static com.namazustudios.socialengine.jnlua.LuaType.TABLE;
import static com.namazustudios.socialengine.rt.lua.Constants.*;
import static com.namazustudios.socialengine.rt.manifest.model.Type.*;

public class CopyCollectionConverter<ObjectT> implements TypedConverter<ObjectT> {

    @Override
    public <T> T convertLuaValue(final LuaState luaState, final int index, final Class<T> formalType) {
        if (Iterable.class.isAssignableFrom(formalType)) {
            final List<?> proxyList = getInstance().convertLuaValue(luaState, index, List.class);
            return (T) new ArrayList<Object>(proxyList);
        } else if (Map.class.isAssignableFrom(formalType)) {
            final Map<?, ?> proxyMap = getInstance().convertLuaValue(luaState, index, Map.class);
            return (T) new LinkedHashMap<Object, Object>(proxyMap);
        } else if (Object.class.equals(formalType)) {
            if (isArray(luaState, index)) {
                final List<?> proxyList = getInstance().convertLuaValue(luaState, index, List.class);
                T list =  (T) new ArrayList<Object>(proxyList);
                return list;
            } else {
                final Map<?, ?> proxyMap = getInstance().convertLuaValue(luaState, index, Map.class);
                T map = (T) new LinkedHashMap<Object, Object>(proxyMap);
                return map;
            }
        } else if (Object[].class.equals(formalType)) {
            final Object[] array = copyArray(luaState, index);
            return (T) array;
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

    public Object[] copyArray(final LuaState luaState, final int index) {

        final int top = luaState.getTop();

        try {

            final int length = luaState.rawLen(index);
            final Object[] array = new Object[length];

            for (int i = 0; i < length; i++) {

                luaState.rawGet(index, i + 1);

                try {
                    array[i] = luaState.toJavaObject(-1, Object.class);
                } finally {
                    luaState.pop(1);
                }

            }

            return array;

        } finally {
            luaState.setTop(top);
        }

    }

    @Override
    public void convertJavaObject(final LuaState luaState, final Object object) {
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
            Map.class.isAssignableFrom(formalType) ||
            Iterable.class.isAssignableFrom(formalType) ||
            Object[].class.equals(formalType) ||
            Object.class.equals(formalType)
        );
    }

}
