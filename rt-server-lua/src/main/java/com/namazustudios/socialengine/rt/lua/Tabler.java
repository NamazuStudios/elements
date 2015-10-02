package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;

import java.util.Collection;
import java.util.Map;

/**
 * A simple interface used to convert Java collection objects to lua tables.
 *
 * Created by patricktwohig on 10/1/15.
 */
public interface Tabler {

    /**
     * Converts the given {@link Collection} to a lua table, and pushes it on the stack.
     *
     * @param luaState
     * @param collection the collection
     */
    void push(LuaState luaState, Collection<?> collection);


    /**
     * Converts the given {@link Map} to a lua table, and pushes it on the stack.
     *
     * @param luaState
     * @param map the collection
     */
    void push(LuaState luaState, Map<?, ?> map);

}
