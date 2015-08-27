package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures that, over the course of method call, the lua stack is popped back to the original state
 * when the stack was created even when an exception is thrown.
 *
 * Created by patricktwohig on 8/27/15.
 */
public class StackProtector implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(StackProtector.class);

    private final LuaState luaState;

    private final int absoluteIndex;

    private int returnCount = 0;

    public StackProtector(LuaState luaState) {
        this.luaState = luaState;
        absoluteIndex = luaState.getTop();
    }

    public int ret(final int returnCount) {

        if (returnCount <= 0) {
            throw new IllegalArgumentException("return count must be positive.");
        } else if (this.returnCount == 0) {
            this.returnCount = returnCount;
        } else {
            throw new IllegalStateException("return count already set: " + returnCount);
        }

        return returnCount;

    }

    void clearReturnCount() {
        returnCount = -1;
    }

    @Override
    public void close() {

        final int newStackTop = Math.min(luaState.getTop(), returnCount + absoluteIndex);

        if (luaState.getTop() == newStackTop) {
            LOG.debug("Stack consistent.");
        } else {
            LOG.debug("Lua stack inconsistent Expected {}.  Actual {}", newStackTop, luaState.getTop());
            luaState.setTop(newStackTop);
        }

    }

}
