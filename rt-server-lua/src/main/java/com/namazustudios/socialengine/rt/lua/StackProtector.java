//package com.namazustudios.socialengine.rt.lua;
//
//import com.naef.jnlua.LuaState;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Ensures that, over the course of method call, the lua stack is popped back to the original state
// * when the stack was created even when an exception is thrown.
// *
// * Created by patricktwohig on 8/27/15.
// */
//public class StackProtector implements AutoCloseable {
//
//    private static final Logger logger = LoggerFactory.getLogger(StackProtector.class);
//
//    private final LuaState luaState;
//
//    private int absoluteIndex;
//
//    public StackProtector(final LuaState luaState) {
//        this(luaState, luaState.getTop());
//    }
//
//    public StackProtector(final LuaState luaState, final int absoluteIndex) {
//        this.luaState = luaState;
//        this.absoluteIndex = absoluteIndex;
//        logger.trace("Stack top {} -> {}", luaState.getTop(), absoluteIndex);
//    }
//
//    public int adjustAbsoluteIndex(final int delta) {
//        logger.trace("Setting stack top {} -> {}", absoluteIndex, delta + absoluteIndex);
//        return absoluteIndex = Math.max(0, delta + absoluteIndex);
//    }
//
//    public int setAbsoluteIndex(final int absoluteIndex) {
//        logger.trace("Setting stack top {} -> {}", this.absoluteIndex, absoluteIndex);
//        return this.absoluteIndex = Math.max(0, absoluteIndex);
//    }
//
//    @Override
//    public void close() {
//        logger.trace("Restoring Lua stack {} -> {}", luaState.getTop(), absoluteIndex);
//        luaState.setTop(absoluteIndex);
//    }
//
//}
