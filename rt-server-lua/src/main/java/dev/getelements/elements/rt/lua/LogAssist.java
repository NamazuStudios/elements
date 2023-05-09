package dev.getelements.elements.rt.lua;

import dev.getelements.elements.jnlua.LuaRuntimeException;
import dev.getelements.elements.jnlua.LuaStackTraceElement;
import dev.getelements.elements.jnlua.LuaState;
import dev.getelements.elements.rt.Resource;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

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
     * Logs an exception with the supplied message as well as dumps the lua stack.  The exception is returned
     * so it can (possibly) be re-thrown.
     *
     * @param throwable
     * @param message
     */
    public void error(final String message, final Throwable throwable) {
        final LuaState luaState = luaStateSupplier.get();
        if (luaState != null) dumpStackError(luaState, message, throwable);
    }

    /**
     * Dumps a specific {@link LuaState}'s stack to the log.  The provided message is logged with the stack tracce
     * for the supplied {@link LuaState}.  Useful for debugging the stack of a specific coroutine.
     *
     * @param luaState the {@link LuaState} object
     * @param msg the message to log along side the stack trace
     * @param throwable an instance of {@Link Throwable} to log.
     */
    private void dumpStackError(final LuaState luaState, final String msg, final Throwable throwable) {

        final Logger logger = loggerSupplier.get();

        if (logger != null && logger.isErrorEnabled()) {
            final String stackTrace = buildStackTrace(luaState, throwable);
            logger.error("{}\n{}\n{}", msg, throwable.getMessage(), stackTrace, throwable);
        }

    }

    private String buildStackTrace(final LuaState luaState, final Throwable throwable) {

        final StringBuilder stringBuilder = new StringBuilder();

        final LuaStackTraceElement[] luaStackTrace = (throwable instanceof LuaRuntimeException) ?
            ((LuaRuntimeException) throwable).getLuaStackTrace() : null;

        if (luaStackTrace != null && luaStackTrace.length > 0) {

            stringBuilder.append("Lua Call Stack\n");

            for (int i = 0; i < luaStackTrace.length; ++i) {
                stringBuilder.append("  ")
                             .append(luaStackTrace[i].toString())
                             .append('\n');
            }

        }

        if (luaState.getTop() != 0) {
            final int top = luaState.getTop();

            stringBuilder.append("Lua VM Stack:\n");

            walkCopyOfVMStack(luaState, i -> {
                stringBuilder.append("  Element ")
                    .append(i).append(" ")
                    .append(luaState.type(i)).append(" ")
                    .append(luaState.toString(i))
                    .append('\n');
            });

        }

        return stringBuilder.toString();

    }

    public List<String> getStack() {


        final LuaState luaState = luaStateSupplier.get();
        if (luaState == null) return Collections.emptyList();

        final List<String> stack = new ArrayList<>();
        walkCopyOfVMStack(luaState, i -> stack.add(luaState.type(i) + " - " + luaState.toString(i)));
        return stack;

    }

    private void walkCopyOfVMStack(final LuaState luaState, final IntConsumer indexConsumer) {

        final int top = luaState.getTop();

        for (int i = 1; i <= top; ++i) {
            luaState.pushValue(i);
        }

        final int newTop = luaState.getTop();

        try {
            IntStream.range(top, newTop).forEach(indexConsumer);
        } finally {
            luaState.pop(newTop - top);
        }

    }

}
