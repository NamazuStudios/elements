package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Resource;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used to assist logging operations with the {@link LuaState}.  Enables things like Lua stack dumps etc.
 */
public class LogAssist {

    private final Supplier<Logger> loggerSupplier;

    private final Supplier<LuaState> luaStateSupplier;

    public LogAssist(final Supplier<Logger> loggerSupplier, final Supplier<LuaState> luaStateSupplier) {
        this.loggerSupplier = loggerSupplier;
        this.luaStateSupplier = luaStateSupplier;
    }

    /**
     * Dumps the Lua stack to the log.
     */
    public void dumpStackError() {
        dumpStackError("Lua Stack:");
    }

    /**
     * Dumps this {@link Resource}'s {@link LuaState} to the log.
     *
     * {@see {@link #dumpStackError(LuaState, String)}}.
     *
     */
    public void dumpStackError(final String msg) {
        final LuaState luaState = luaStateSupplier.get();
        if (luaState != null) dumpStackError(luaState, msg);
    }

    /**
     * Dumps a specific {@link LuaState}'s stack to the log.  The provided message is logged with the stack tracce
     * for the supplied {@link LuaState}.  Useful for debugging the stack of a specific coroutine.
     *
     * @param luaState the {@link LuaState} object
     * @param msg the message to log along side the stack trace
     */
    public void dumpStackError(final LuaState luaState, final String msg) {

        final Logger logger = loggerSupplier.get();

        if (logger != null && logger.isErrorEnabled()) {
            final String stackTrace = buildStackTrace(luaState);
            logger.error("{}\n{}", msg, stackTrace);
        }

    }

    /**
     * Dumps a specific {@link LuaState}'s stack to the log.  The provided message is logged with the stack tracce
     * for the supplied {@link LuaState}.  Useful for debugging the stack of a specific coroutine.
     *
     * @param luaState the {@link LuaState} object
     * @param msg the message to log along side the stack trace
     * @param throwable an instance of {@Link Throwable} to log.
     */
    public void dumpStackError(final LuaState luaState, final String msg, final Throwable throwable) {

        final Logger logger = loggerSupplier.get();

        if (logger != null && logger.isErrorEnabled()) {
            final String stackTrace = buildStackTrace(luaState);
            logger.error("{}\n{}", msg, stackTrace, throwable);
        }

    }

    /**
     * Logs an exception with the supplied message as well as dumps the lua stack.  The exception is returned
     * so it can (possibly) be re-thrown.
     *
     * @param throwable
     * @param message
     * @param <ThrowableT>
     * @return
     */
    public <ThrowableT extends Throwable>
    ThrowableT error(final String message, final ThrowableT throwable) {
        final LuaState luaState = luaStateSupplier.get();
        if (luaState != null) dumpStackError(luaState, message, throwable);
        return throwable;
    }

    private String buildStackTrace(final LuaState luaState) {

        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 1; i <= luaState.getTop(); ++i) {
            stringBuilder.append("  Element ")
                    .append(i).append(" ")
                    .append(luaState.type(i)).append(" ")
                    .append(luaState.toString(i))
                    .append('\n');
        }

        return stringBuilder.toString();

    }

    public List<String> getStack() {
        final List<String> stack = new ArrayList<>();
        final LuaState luaState = luaStateSupplier.get();
        for (int i = 1; i <= luaState.getTop(); ++i) stack.add(luaState.type(i) + " - " + luaState.toString(i));
        return stack;
    }

}
