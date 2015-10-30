package com.namazustudios.socialengine.rt.lua;

import com.google.common.base.Splitter;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import com.namazustudios.socialengine.rt.internal.InternalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLuaResource.class);

    /**
     * The print function name.  This hijacks the "regular" print function and diverts its output
     * to the script's log.  The script's Log is actually backed by slf4j
     */
    public static final String PRINT_FUNCTION = "print";

    /**
     * The name of a global table where the Lua code interacts with the
     * underlying server APIs.
     */
    public static final String NAMAZU_RT_TABLE = "namazu_rt";

    /**
     * The name of the namazu_rt close() function which will be called just before the resource
     * is closed.  This is useful in case the underlying lua script needs to release or free
     * any resources before it is destroyed.
     */
    public static final String CLOSE_FUNCTION = "close";

    /**
     * A table housing the response codes as defined by {@link ResponseCode#getCode()}
     */
    public static final String RESPONSE_CODE = "response_code";

    /**
     * This is the table name under namazu_rt that defines the init parameters for the script.
     */
    public static final String INIT_PARAMS = "init_params";

    /**
     * Constant to designate the server.coroutine table.  This is a set of coroutinee
     * functions managed by the server.
     */
    public static final String COROUTINE_TABLE = "coroutine";

    /**
     * The name of the server.coroutine.create() function.
     */
    public static final String COROUTINE_CREATE_FUNCTION = "create";

    /**
     * A registry table for server threads.  This is stored in the lua registry and is not
     * visible to the Lua source code at all.
     */
    public static final String SERVER_THREADS_TABLE = "NAMAZU_RT_SERVER_THREADS";

    /**
     * The table underneath the namazu_rt table which handles requests.
     */
    public static final String REQUEST_TABLE = "request";

    /**
     * A key on the services table to expose  an instance of this type to the underlying Lua script.  This allows
     * the underlying script to perform functions such as getting the current path, or posting events to subscribers.
     */
    public static final String THIS_INSTANCE = "resource";

    /**
     * The "package" table.  See the Lua manual for what this is used for.
     */
    public static final String PACKAGE_TABLE = "package";

    /**
     * The "package.searchers" table.  See the Lua manual for what this is used for.
     */
    public static final String PACKAGE_SEARCHERS_TABLE = "searchers";

    /**
     * Exposes the instance of {@link IocResolver} which the underlying script can use to resolve dependencies
     * such as other instances of {@link Resource}, {@link EdgeServer}, and {@link InternalServer}.
     */
    public static final String IOC_INSTANCE = "ioc";

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

    private Logger scriptLog = LOG;

    /**
     * Creates a new thread managed by the server.  This returns to the calling code
     * the thread that was created.
     */
    private final JavaFunction serverStartCoroutine = new JavaFunction() {
        @Override
        public int invoke(final LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                if (!luaState.isFunction(-1)) {
                    dumpStack();
                    throw new IllegalArgumentException("server.coroutine.create() must be passed a function");
                }

                final UUID uuid = UUID.randomUUID();

                luaState.newThread();
                luaState.getField(LuaState.REGISTRYINDEX, SERVER_THREADS_TABLE);
                luaState.pushValue(-2);
                luaState.setField(-2, uuid.toString());
                luaState.pop(1);

                getScriptLog().info("Created coroutine {}", uuid);
                return stackProtector.setAbsoluteIndex(1);

            }
        }
    };

    /**
     * Redirects the print function to the logger returned by {@link #getScriptLog()}.
     */
    private final JavaFunction printToScriptLog = new JavaFunction() {
        @Override
        public int invoke(LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {
                final StringBuffer stringBuffer = new StringBuffer();

                for (int i = 1; i <= luaState.getTop(); ++i) {
                    stringBuffer.append(luaState.toJavaObject(i, String.class));
                }

                getScriptLog().info("{}", stringBuffer.toString());
                return stackProtector.setAbsoluteIndex(0);
            }
        }
    };

    private final JavaFunction classpathSearcher = new JavaFunction() {
        @Override
        public int invoke(final  LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                if (!luaState.isString(-1)) {
                    luaState.pushString("module name must be a string");
                    return 1;
                }

                final String moduleName = luaState.checkString(-1);
                final ClassLoader classLoader = AbstractLuaResource.class.getClassLoader();
                final URL resourceURL = classLoader.getResource(moduleName + ".lua");

                luaState.setTop(0);

                if (resourceURL == null) {
                    luaState.pushString(moduleName + " not found on classpath");
                } else {
                    luaState.pushJavaFunction(classpathLoader);
                    luaState.pushJavaObject(resourceURL);
                }

                return stackProtector.setAbsoluteIndex(2);

            }
        }
    };

    private final JavaFunction classpathLoader = new JavaFunction() {
        @Override
        public int invoke(final LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                final URL resourceURL = luaState.checkJavaObject(-1, URL.class);
                final String simpleFileName = simlifyFileName(resourceURL.getFile());
                getScriptLog().debug("Loading module from {}", resourceURL);

                try (final InputStream inputStream = resourceURL.openStream())  {
                    luaState.load(inputStream, simpleFileName, "bt");
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }

                luaState.remove(-2);
                luaState.remove(-2);
                luaState.call(0, 1);

                return stackProtector.setAbsoluteIndex(1);

            }
        }
    };

    /**
     * Creates an instance of {@link AbstractLuaResource} with the given {@link LuaState}
     * type.j
     *
     * If instantion fails, it is the responsilbity of the caller to deallocate the {@link LuaState}
     * object.  If the constructor completes without error, then the caller does not need to close
     * the state as it will be handled by this object's {@link #close()} method.
     *
     * @param luaState the luaState
     */
    public AbstractLuaResource(final LuaState luaState,
                               final IocResolver iocResolver,
                               final Tabler tabler) {
        this.luaState = luaState;
        this.iocResolver = iocResolver;
        this.tabler = tabler;
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
            setupModuleSearchers();
            setupFunctionOverrides();
        }
    }

    private void setupServerCoroutineTable() {
        // Places a table in the registry to hold the currently running threads.
        luaState.newTable();
        luaState.setField(LuaState.REGISTRYINDEX, SERVER_THREADS_TABLE);
    }

    private void setupNamazuRTTable() {

        // Creates a new table to hold the server table functions.  This will ultimately
        // be the namauz_rt table.
        luaState.newTable();

        // Creates a place for server.request.  This is an empty table which is used to house
        // request handler functions.
        luaState.newTable();
        luaState.setField(-2, REQUEST_TABLE);

        // Creates a place for the init_params.  By default this is just an empty table and
        // will be overridden by a call to this.init()
        luaState.newTable();
        luaState.setField(-2, INIT_PARAMS);

        // Creates a place for hte response_code constants.
        luaState.newTable();

        for (final ResponseCode responseCode : ResponseCode.values()) {
            luaState.pushInteger(responseCode.getCode());
            luaState.setField(-2, responseCode.toString());
        }

        luaState.setField(-2, RESPONSE_CODE);

        // Creates a table for server.coroutine.  This houses code for
        // server-managed coroutines that will automatically be activated
        // on every update.

        luaState.newTable();
        luaState.pushJavaFunction(serverStartCoroutine);
        luaState.setField(-2, COROUTINE_CREATE_FUNCTION);
        luaState.setField(-2, COROUTINE_TABLE);

        // Sets up the services table which references this and the IOC resolver
        // instance.

        luaState.pushJavaObject(this);
        luaState.setField(-2, THIS_INSTANCE);

        luaState.pushJavaObject(iocResolver);
        luaState.setField(-2, IOC_INSTANCE);

        // Finally sets the server table to be in the global space
        luaState.setGlobal(NAMAZU_RT_TABLE);

    }

    private void setupModuleSearchers() {
        luaState.getGlobal(PACKAGE_TABLE);
        luaState.getField(-1, PACKAGE_SEARCHERS_TABLE);
        luaState.pushJavaFunction(classpathSearcher);
        luaState.rawSet(-2, luaState.rawLen(-1) + 1);
        luaState.pop(2);
    }

    private void setupFunctionOverrides() {
        // Lastly we hijack the standard lua print function to redirect
        // to the underlying logging system
        luaState.pushJavaFunction(printToScriptLog);
        luaState.setGlobal(PRINT_FUNCTION);
    }

    @Override
    public void init(final Map<String, Object> parameters) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.getGlobal(NAMAZU_RT_TABLE);
            tabler.push(luaState, parameters);
            luaState.setField(-2, INIT_PARAMS);
        }
    }

    @Override
    protected void doUpdate(double deltaTime) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            runManagedCoroutines(deltaTime);
        }
    }

    private void runManagedCoroutines(double deltaTime) {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            luaState.getField(LuaState.REGISTRYINDEX, SERVER_THREADS_TABLE);

            final int threadTableIndex = luaState.absIndex(-1);
            final List<String> threadsToReap = new ArrayList<>();

            luaState.pushNil();
            while (luaState.next(threadTableIndex)) {

                if (!luaState.isThread(-1)) {
                    luaState.pop(1);
                    continue;
                }

                final int threadStatus = luaState.status(-1);

                if ((threadStatus == LuaState.YIELD) || (threadStatus == luaState.OK)) {
                    luaState.pushNumber(deltaTime);
                    try {
                        final int returnCount = luaState.resume(-2, 1);
                        luaState.pop(returnCount);
                    } catch (LuaRuntimeException ex) {
                        dumpStack();
                        dumpStack(ex);
                    }
                } else {
                    threadsToReap.add(luaState.checkString(-2));
                }

                luaState.pop(1);

            }

            for (final String uuid : threadsToReap) {
                getScriptLog().info("Reaping thread {}.", uuid);
                luaState.pushNil();
                luaState.setField(threadTableIndex, uuid);
            }

            luaState.pop(1);

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
     * @throws {@link NotFoundException} if methodName name is not found
     *
     */
    protected void pushRequestHandlerFunction(final String methodName) {

        try (final StackProtector stackProtector = new StackProtector(luaState, 1)) {

            luaState.getGlobal(NAMAZU_RT_TABLE);

            // The first two checks shouldn't fail, unless somebody has seriously
            // hosed the lua script backing this resource.

            if (!luaState.isTable(-1)) {
                getScriptLog().error("Unable to find table {}", NAMAZU_RT_TABLE);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            luaState.getField(-1, REQUEST_TABLE);
            luaState.remove(-2);  // pops namazu_rt

            if (!luaState.isTable(-1)) {
                getScriptLog().error("Unable to find table {}.{}", NAMAZU_RT_TABLE, REQUEST_TABLE);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            // Here's where the failures can be considered "normal" in that somebody could
            // have just forgotten to define a handler.

            luaState.getField(-1, methodName);
            luaState.remove(-2); // pops request

            if (!luaState.isFunction(-1)) {
                getScriptLog().warn("Unable to find function {}.{}.{}", NAMAZU_RT_TABLE, REQUEST_TABLE, methodName);
                throw new NotFoundException(methodName + " doest not exist for " + this);
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
     * Invokes {@link LuaState#close()} and removes any resources from memory.  Once
     * this is called, this resource may not be reused.
     *
     * @see {@link Resource#close()}
     *
     */
    @Override
    public void close() {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            luaState.getGlobal(NAMAZU_RT_TABLE);   // Pushes namazu_rt
            luaState.getField(-1, CLOSE_FUNCTION); // pushes close() (if it exists)
            luaState.remove(-2);                   // pops namazu_rt

            if (!luaState.isNil(-1) && !luaState.isFunction(-1)) {
                getScriptLog().warn("{}.{} is not a function.", NAMAZU_RT_TABLE, CLOSE_FUNCTION);
            } else if (luaState.isFunction(-1)) {
                luaState.call(0,0);
            }

        } catch (final Exception ex) {
            dumpStack();
            getScriptLog().error("Caught exception invoking script {}() function", CLOSE_FUNCTION, ex);
            throw new InternalException(ex);
        } finally {
            luaState.close();
        }

    }

}
