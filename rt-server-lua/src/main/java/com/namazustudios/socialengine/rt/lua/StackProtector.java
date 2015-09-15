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
        LOG.trace("Stack top {}", absoluteIndex);
    }

    public int ret(final int returnCount) {

        LOG.debug("Setting return count to {}.  New final absolute stack top {}", returnCount + returnCount);

        if (returnCount <= 0) {
            throw new IllegalArgumentException("return count must be positive.");
        } else if (this.returnCount == 0) {
            this.returnCount = returnCount;
        } else {
            throw new IllegalStateException("return count already set: " + returnCount);
        }

        return returnCount;

    }

    @Override
    public void close() {

        final int newStackTop = Math.min(luaState.getTop(), returnCount + absoluteIndex);

        if (luaState.getTop() < (returnCount + absoluteIndex)) {
            LOG.debug("Stack top {}.  Expected new top {}.  Did you forget to push the return value?", newStackTop);
        }

        if (luaState.getTop() == newStackTop) {
            LOG.trace("Stack consistent.");
        } else {
            LOG.trace("Lua stack inconsistent Expected {}.  Actual {}", newStackTop, luaState.getTop());
            luaState.setTop(newStackTop);
        }

    }

}
