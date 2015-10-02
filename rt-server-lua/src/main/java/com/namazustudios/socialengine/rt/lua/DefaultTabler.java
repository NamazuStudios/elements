package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This class will serialize an instance of {@Link Collection} or {@link Map} to a lua table
 * and push it on the stack.  Strings and numbers are pushed as they normally would be pushed.
 *
 * {@link Map} and {@link Collection} instances are converted to lua tables.  The serializer
 * properly handles circular references and reflects those changes in Lua.
 *
 * This instance is not thread safe, but may be used repeatedly (even if an exception is thrown).  This
 * makes use of the lua stack to perform serialization and uses the {@link StackProtector} to ensure
 * that the calls are made with consistency.
 *
 * Created by patricktwohig on 10/1/15.
 */
public class DefaultTabler implements Tabler {

    private final Map<Object, Integer> references = new IdentityHashMap<>();

    @Override
    public void push(final LuaState luaState, final Collection<?> collection) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.newTable();
            references.put(collection, luaState.getTop());
            toTable(luaState, collection);
            stackProtector.adjustAbsoluteIndex(1);
        } finally {
            references.clear();
        }
    }

    @Override
    public void push(final LuaState luaState, final Map<?, ?> map) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.newTable();
            references.put(map, luaState.getTop());
            toTable(luaState, map);
            stackProtector.adjustAbsoluteIndex(1);
        } finally {
            references.clear();
        }
    }

    private void setObject(final LuaState luaState, final int index, final Object object) {
        if (object instanceof Number) {
            setNumber(luaState, index, (Number)object);
        } else if (object instanceof String) {
            setString(luaState, index, (String)object);
        } else if (object instanceof  Collection<?>) {
            setCollection(luaState, index, (Collection<?>) object);
        } else if (object instanceof Map<?,?>) {
            setMap(luaState, index, (Map<?,?>) object);
        } else {
            setObjectProxy(luaState, index, object);
        }
    }

    private void setNumber(final LuaState luaState, final int index, final Number number) {
        luaState.pushNumber(index);
        luaState.pushNumber(number.doubleValue());
        luaState.setTable(-3);
    }

    private void setString(final LuaState luaState, final int index, final String string) {
        luaState.pushNumber(index);
        luaState.pushString(string);
        luaState.setTable(-3);
    }

    private void setCollection(final LuaState luaState, final int index, final Collection<?> collection) {

        luaState.pushNumber(index);

        if (references.containsKey(collection)) {
            luaState.pushValue(references.get(collection));
        } else {
            luaState.newTable();
            references.put(collection, luaState.getTop());
            toTable(luaState, collection);
        }

        luaState.setTable(-3);

    }

    private void setMap(final LuaState luaState, final int index, final Map<?,?> map) {

        luaState.pushNumber(index);

        if (references.containsKey(map)) {
            luaState.pushValue(references.get(map));
        } else {
            luaState.newTable();
            references.put(map, luaState.getTop());
            toTable(luaState, map);
        }

        luaState.setTable(-3);

    }

    private void setObjectProxy(final LuaState luaState, final int index, final Object object) {
        luaState.pushNumber(index);
        luaState.pushJavaObject(object);
        luaState.setTable(-3);
    }

    private void setObject(final LuaState luaState, final String key, final Object object) {
        if (object instanceof Number) {
            setNumber(luaState, key, (Number)object);
        } else if (object instanceof String) {
            setString(luaState, key, (String) object);
        } else if (object instanceof  Collection<?>) {
            setCollection(luaState, key, (Collection<?>) object);
        } else if (object instanceof Map<?,?>) {
            setMap(luaState, key, (Map<?,?>)object);
        } else {
            setObjectProxy(luaState, key, object);
        }
    }

    private void setNumber(final LuaState luaState, final String key, final Number number) {
        luaState.pushNumber(number.doubleValue());
        luaState.setField(-2, key);
    }

    private void setString(final LuaState luaState, final String key, final String string) {
        luaState.pushString(string);
        luaState.setField(-2, key);
    }

    private void setCollection(final LuaState luaState, final String key, final Collection<?> collection) {

        if (references.containsKey(collection)) {
            luaState.pushValue(references.get(collection));
        } else {
            luaState.newTable();
            references.put(collection, luaState.getTop());
            toTable(luaState, collection);
        }

        luaState.setField(-2, key);

    }

    private void setMap(final LuaState luaState, final String key, final Map<?,?> map) {

        if (references.containsKey(map)) {
            luaState.pushValue(references.get(map));
        } else {
            luaState.newTable();
            references.put(map, luaState.getTop());
            toTable(luaState, map);
        }

        luaState.setField(-2, key);

    }

    private void setObjectProxy(final LuaState luaState, final String key, final Object object) {
        luaState.pushJavaObject(object);
        luaState.setField(-2, key);
    }

    private void toTable(final LuaState luaState, final Collection<?> collection) {

        int index = 0;
        for (final Object element : collection) {
            // Lua is one-indexed
            setObject(luaState, ++index, element);
        }

    }

    private void toTable(final LuaState luaState, final Map<?,?> map) {
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            setObject(luaState, entry.getKey().toString(), entry.getValue());
        }
    }

}
