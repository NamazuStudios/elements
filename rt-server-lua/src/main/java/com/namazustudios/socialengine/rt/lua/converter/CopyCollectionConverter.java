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
            return (T) copyList(luaState, index);
        } else if (Map.class.isAssignableFrom(formalType)) {
            return (T) copyMap(luaState, index);
        } else if (Object.class.equals(formalType)) {
            return isArray(luaState, index) ? (T) copyList(luaState, index) : (T) copyMap(luaState, index);
        } else if (Object[].class.equals(formalType)) {
            return (T) copyArray(luaState, index);
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

    private Map<?, ?> copyMap(final LuaState luaState, final int index) {

        final int abs = luaState.absIndex(index);
        final Map<Object, Object> out = new LinkedHashMap<>();

        luaState.pushJavaFunction(l -> {
            final LuaType luaType = l.type(1);
            final Map<?, ?> proxyMap = getInstance().convertLuaValue(l, 1, Map.class);
            out.putAll(proxyMap);
            return 0;
        });
        luaState.pushValue(abs);
        luaState.call(1, 0);

        return out;

    }

    private List<?> copyList(final LuaState luaState, final int index) {

        final int abs = luaState.absIndex(index);
        final List<Object> out = new ArrayList<>();

        luaState.pushJavaFunction(l -> {
            final List<?> proxyList = getInstance().convertLuaValue(l, 1, List.class);
            out.addAll(proxyList);
            return 0;
        });
        luaState.pushValue(abs);
        luaState.call(1, 0);

        return out;

    }

    private Object[] copyArray(final LuaState luaState, final int index) {

        final int top = luaState.getTop();
        final int abs = luaState.absIndex(index);

        try {

            final int length = luaState.rawLen(index);
            final Object[] array = new Object[length];

            luaState.pushJavaFunction(l -> {

                for (int i = 0; i < length; i++) {

                    luaState.rawGet(1, i + 1);

                    try {
                        array[i] = luaState.toJavaObject(-1, Object.class);
                    } finally {
                        luaState.pop(1);
                    }

                }

                return 0;

            });
            luaState.pushValue(abs);
            luaState.call(1, 0);

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

            luaState.pushJavaFunction(l -> {
                l.newTable();

                map.forEach((k, v) -> {
                    l.pushJavaObject(k);
                    l.pushJavaObject(v);
                    l.setTable(-3);
                });

                l.newTable();
                l.pushString(OBJECT.value);
                l.setField(-2, MANIFEST_TYPE_METAFIELD);
                l.setMetatable(-2);
                l.setTop(1);

                return 1;
            });

            luaState.call(0, 1);

        } else if (object instanceof Iterable) {

            luaState.pushJavaFunction(l -> {

                int index = 0;
                final Iterable<?> list = (Iterable<?>)object;
                final Iterator<?> listIterator = list.iterator();

                l.newTable();

                while (listIterator.hasNext()) {
                    final Object element = listIterator.next();
                    l.pushJavaObject(element);
                    l.rawSet(-2, ++index);
                }

                l.newTable();
                l.pushString(ARRAY.value);
                l.setField(-2, MANIFEST_TYPE_METAFIELD);
                l.setMetatable(-2);
                l.setTop(1);

                return 1;

            });

            luaState.call(0, 1);

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
