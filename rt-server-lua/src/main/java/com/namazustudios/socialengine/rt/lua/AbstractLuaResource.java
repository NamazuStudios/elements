package com.namazustudios.socialengine.rt.lua;

import com.google.common.base.Splitter;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AbstractResource;
import com.namazustudios.socialengine.rt.Container;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * The abstract {@link Resource} type backed by a Lua script.  This uses the JNLua implentation
 * to drive the script.
 *
 * Note that this eschews the traditional static {@link Logger} instance, and creates an individual
 * instance named for the script itself.
 *
 * Created by patricktwohig on 8/25/15.
 */
public abstract class AbstractLuaResource extends AbstractResource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLuaResource.class);

    /**
     * Simplifies the file name for the sake of better error reporting.
     *
     * @param fileName the fileName
     * @return the simplified file name.
     */
    public static String simlifyFileName(final String fileName) {

        final List<String> pathComponents = Splitter.on(File.separator)
                                                    .trimResults()
                                                    .omitEmptyStrings()
                                                    .splitToList(fileName);

        final int listSize = pathComponents.size();
        return listSize == 0 ? fileName : pathComponents.get(listSize - 1);

    }

    private final LuaState luaState;

    private final IocResolver iocResolver;

    private final Tabler tabler;

    private final CoroutineManager coroutineManager;

    private final ClasspathModuleLoader classpathModuleLoader;

    private Logger scriptLog = logger;

    /**
     * Redirects the print function to the logger returned by {@link #getScriptLog()}.
     */
    private final JavaFunction printToScriptLog = new ScriptLogger(s -> logger.info("{}", s));

    /**
     * Creates an instance of {@link AbstractLuaResource} with the given {@link LuaState}
     * type.j
     *
     * If instantiation fails, it is the responsiblity of the caller to deallocate the {@link LuaState}
     * object.  If the constructor completes without error, then the caller does not need to close
     * the state as it will be handled by this object's {@link #close()} method.
     *
     * @param luaState the luaState
     */
    public AbstractLuaResource(final LuaState luaState,
                               final IocResolver iocResolver,
                               final Tabler tabler,
                               final Container<?> container) {
        this.luaState = luaState;
        this.iocResolver = iocResolver;
        this.tabler = tabler;
        coroutineManager = new CoroutineManager(this, container);
        classpathModuleLoader = new ClasspathModuleLoader(this);
    }

    /**
     * Loads and runs a Lua script from the given {@link InputStream} instance.  The name
     * supplied is useful for debugging and should match the name of the file from which
     * the script was loaded.
     *
     * @param inputStream the input stream
     * @param name the name of the script (useful for debugging)
     * @throws IOException
     */
    public void loadAndRun(final InputStream inputStream, final String name) throws IOException {
        try (final StackProtector stackProtector = new StackProtector(luaState, 0)) {

            luaState.openLibs();

            scriptLog = LoggerFactory.getLogger(name);

            setupScriptGlobals();
            coroutineManager.setup();
            classpathModuleLoader.setup();

            luaState.load(inputStream, name, "bt");
            getScriptLog().debug("Loaded lua script.", luaState);

            luaState.call(0, 0);
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

        // Creates a new table to hold the server table functions.  This will ultimately
        // be the namauz_rt table.
        luaState.newTable();

        // Creates a place for server.request.  This is an empty table which is used to house
        // request handler functions.
        luaState.newTable();
        luaState.setField(-2, Constants.REQUEST_TABLE);

        // Creates a place for the init_params.  By default this is just an empty table and
        // will be overridden by a call to this.init()
        luaState.newTable();
        luaState.setField(-2, Constants.INIT_PARAMS);

        // Creates a place for hte response_code constants.
        luaState.newTable();

        for (final ResponseCode responseCode : ResponseCode.values()) {
            luaState.pushInteger(responseCode.getCode());
            luaState.setField(-2, responseCode.toString());
        }

        luaState.setField(-2, Constants.RESPONSE_CODE);

        // Sets up the services table which references this and the IOC resolver
        // instance.

        luaState.pushJavaObject(this);
        luaState.setField(-2, Constants.THIS_INSTANCE);

        luaState.pushJavaObject(iocResolver);
        luaState.setField(-2, Constants.IOC_INSTANCE);

        // Finally sets the server table to be in the global space
        luaState.setGlobal(Constants.NAMAZU_RT_TABLE);

    }

    private void setupFunctionOverrides() {
        // Lastly we hijack the standard lua print function to redirect
        // to the underlying logging system
        luaState.pushJavaFunction(printToScriptLog);
        luaState.setGlobal(Constants.PRINT_FUNCTION);
    }

    @Override
    public void init(final Map<String, Object> parameters) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.getGlobal(Constants.NAMAZU_RT_TABLE);
            tabler.push(luaState, parameters);
            luaState.setField(-2, Constants.INIT_PARAMS);
        }
    }

    /**
     * Pushes the request handler function for the given method name.
     *
     * Any other intermediate variables are popped on the stack.  The end result of this
     * call should result in only the requets handler table being pushed.
     *
     * @param methodName the method name
     *
     * @throws {@link MethodNotFoundException} if methodName name is not found
     *
     */
    protected void pushRequestHandlerFunction(final String methodName) {

        try (final StackProtector stackProtector = new StackProtector(luaState, 1)) {

            luaState.getGlobal(Constants.NAMAZU_RT_TABLE);

            // The first two checks shouldn't fail, unless somebody has seriously
            // hosed the lua script backing this resource.

            if (!luaState.isTable(-1)) {
                getScriptLog().error("Unable to find table {}", Constants.NAMAZU_RT_TABLE);
                throw new MethodNotFoundException(methodName + " doest not exist for " + this);
            }

            luaState.getField(-1, Constants.REQUEST_TABLE);
            luaState.remove(-2);  // pops namazu_rt

            if (!luaState.isTable(-1)) {
                getScriptLog().error("Unable to find table {}.{}", Constants.NAMAZU_RT_TABLE, Constants.REQUEST_TABLE);
                throw new MethodNotFoundException(methodName + " doest not exist for " + this);
            }

            // Here's where the failures can be considered "normal" in that somebody could
            // have just forgotten to define a handler.

            luaState.getField(-1, methodName);
            luaState.remove(-2); // pops request

            if (!luaState.isFunction(-1)) {
                getScriptLog().warn("Unable to find function {}.{}.{}", Constants.NAMAZU_RT_TABLE, Constants.REQUEST_TABLE, methodName);
                throw new MethodNotFoundException(methodName + " doest not exist for " + this);
            }

        }

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
     * Dumps the Lua stack to the log.  The provided message is prepended to the stack trace.
     */
    public void dumpStack(final String msg) {

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
     * Dumps the stack from an instance of {@link LuaRuntimeException}
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
     * Gets the {@link LuaState} backing this {@link AbstractLuaResource}.
     *
     * @return the {@link LuaState} instance
     */
    public LuaState getLuaState() {
        return luaState;
    }

    /**
     * Invokes {@link LuaState#close()} and removes any resources from memory.  Once
     * this is called, this resource may not be reused.
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

}
