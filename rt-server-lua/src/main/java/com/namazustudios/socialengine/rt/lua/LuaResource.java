package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AbstractResource;
import com.namazustudios.socialengine.rt.MethodDispatcher;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import com.namazustudios.socialengine.rt.lua.builtin.JavaObjectBuiltin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The abstract {@link Resource} type backed by a Lua script.  This uses the JNLua implentation
 * to drive the script.
 *
 * Note that this eschews the traditional static {@link Logger} instance, and creates an individual
 * instance named for the script itself.
 *
 * Created by patricktwohig on 8/25/15.
 */
public class LuaResource extends AbstractResource {

    public static final String RESOURCE_BUILTIN = "namazu.resource.this";

    private static final Logger logger = LoggerFactory.getLogger(LuaResource.class);

    private final LuaState luaState;

    private Logger scriptLog = logger;

    /**
     * Redirects the print function to the logger returned by {@link #getScriptLog()}.
     */
    private final JavaFunction printToScriptLog = new ScriptLogger(s -> logger.info("{}", s));

    /**
     * Creates an instance of {@link LuaResource} with the given {@link LuaState}
     * type.j
     *
     * If instantiation fails, it is the responsiblity of the caller to deallocate the {@link LuaState}
     * object.  If the constructor completes without error, then the caller does not need to close
     * the state as it will be handled by this object's {@link #close()} method.
     *
     * @param luaState the luaState
     */
    @Inject
    public LuaResource(final LuaState luaState) {
        this.luaState = luaState;
        installBuiltin(new JavaObjectBuiltin<>(RESOURCE_BUILTIN, this));
    }

    /**
     *
     * Loads and runs a Lua script from the given {@link InputStream} instance.  The name
     * supplied is useful for debugging and should match the name of the file from which
     * the script was loaded.
     *
     * @param inputStream the input stream
     * @param name the name of the module to debug
     *
     * @throws IOException if the loading fails
     */
    public void loadAndRun(final InputStream inputStream, final String name, final Object ... params) throws IOException {
        try (final StackProtector stackProtector = new StackProtector(luaState, 0)) {

            luaState.openLibs();

            scriptLog = LoggerFactory.getLogger(name);

            setupScriptGlobals();

            luaState.load(inputStream, name, "bt");
            getScriptLog().debug("Loaded lua script.", luaState);

            for(final Object param : params) {
                luaState.pushJavaObject(param);
            }

            luaState.call(params.length, 1);
            getScriptLog().debug("Executed lua script.", luaState);

        }
    }

    private void setupScriptGlobals() {
        try (final StackProtector stackProtector = new StackProtector(luaState, 0)) {
            setupServerCoroutineTable();
            setupNamazuRTTable();
            setupFunctionOverrides();
        }
    }

    private void setupServerCoroutineTable() {
        // Places a table in the registry to hold the currently running threads.
        luaState.newTable();
        luaState.setField(LuaState.REGISTRYINDEX, Constants.SERVER_THREADS_TABLE);
    }

    private void setupNamazuRTTable() {

        // Creates a table for the
        luaState.newTable();

        // Adds this resource object as well as the IoC resolver instance where the script
        // may have access to all underlying services

        // Finally sets the server table to be in the global space
        luaState.setGlobal(Constants.NAMAZU_RT_TABLE);

    }

    private void setupFunctionOverrides() {
        // Lastly we hijack the standard lua print function to redirect
        // to the underlying logging system
        luaState.pushJavaFunction(printToScriptLog);
        luaState.setGlobal(Constants.PRINT_FUNCTION);
    }

    /**
     * Gets a special instance of {@link Logger} which the script can use for script logging.
     *
     * @return the {@link Logger} instance
     */
    public Logger getScriptLog() {
        return scriptLog;
    }

    /**
     * Dumps the Lua stack to the log.
     */
    public void dumpStack() {
        dumpStack("Lua Stack:");
    }

    /**
     * Dumps this {@link Resource}'s {@link LuaState} to the log.
     *
     * {@see {@link #dumpStack(LuaState, String)}}.
     *
     */
    public void dumpStack(final String msg) {
        dumpStack(luaState, msg);
    }

    /**
     * Dumps a specific {@link LuaState}'s stack to the log.  The provided message is logged with the stack tracce
     * for the supplied {@link LuaState}.  Useful for debugging the stack of a specific coroutine.
     *
     * @param luaState the {@link LuaState} object
     * @param msg the message to log along side the stack trace
     */
    public void dumpStack(final LuaState luaState, final String msg) {

        if (getScriptLog().isErrorEnabled()) {

            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(msg).append("\n");

            for (int i = 1; i <= luaState.getTop(); ++i) {
                stringBuilder.append("  Element ")
                        .append(i).append(" ")
                        .append(luaState.type(i)).append(" ")
                        .append(luaState.toString(i))
                        .append('\n');
            }

            getScriptLog().error("{}", stringBuilder);

        }

    }


    /**
     * Dumps the stack from an instance of {@link LuaRuntimeException}.
     *
     * @param lre the exception
     */
    public void dumpStack(final LuaRuntimeException lre) {

        getScriptLog().error("Exception running script.", lre);

        try (final StringWriter stringWriter = new StringWriter();
             final PrintWriter printWriter = new PrintWriter(stringWriter) ) {
            lre.printLuaStackTrace(printWriter);
            getScriptLog().error("Lua Stack Trace {} ", stringWriter.getBuffer().toString());
        } catch (IOException ex) {
            getScriptLog().error("Caught exception writing Lua stack trace", lre);
        }

    }

    /**
     * Gets the {@link LuaState} backing this {@link LuaResource}.
     *
     * @return the {@link LuaState} instance
     */
    public LuaState getLuaState() {
        return luaState;
    }

    /**
     * Invokes {@link LuaState#close()} and removes any resources from memory.  After this is called, this
     * {@link LuaResource} may not be reused.
     *
     * @see {@link Resource#close()}
     *
     */
    @Override
    public void close() {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            luaState.getGlobal(Constants.NAMAZU_RT_TABLE);   // Pushes namazu_rt
            luaState.getField(-1, Constants.CLOSE_FUNCTION); // pushes close() (if it exists)
            luaState.remove(-2);                             // pops namazu_rt

            if (!luaState.isNil(-1) && !luaState.isFunction(-1)) {
                getScriptLog().warn("{}.{} is not a function.", Constants.NAMAZU_RT_TABLE, Constants.CLOSE_FUNCTION);
            } else if (luaState.isFunction(-1)) {
                luaState.call(0,0);
            }

        } catch (final Exception ex) {
            dumpStack();
            getScriptLog().error("Caught exception invoking script {}() function", Constants.CLOSE_FUNCTION, ex);
            throw new InternalException(ex);
        } finally {
            luaState.close();
        }

    }

    @Override
    public MethodDispatcher getMethodDispatcher(final String name) {
        return params -> (consumer, throwableConsumer) -> {};
    }

    /**
     * Installs the {@link Builtin} module to this {@link LuaResource} such that the underlying code may make use of it
     * using the require function.
     *
     * @param builtin the {@link Builtin} to install
     */
    public void installBuiltin(final Builtin builtin) {

        final LuaState luaState = getLuaState();

        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.getGlobal(Constants.PACKAGE_TABLE);
            luaState.getField(-1, Constants.PACKAGE_SEARCHERS_TABLE);
            luaState.pushJavaFunction(builtin.getSearcher());
            luaState.rawSet(-2, luaState.rawLen(-1) + 1);
            luaState.pop(2);
        }

    }

}
