package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.Converter;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import com.namazustudios.socialengine.rt.internal.InternalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
public class AbstractLuaResource extends AbstractResource {

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
     * A table which contains java objects that can be used to communicate with the
     * rest of the server.
     */
    public static final String BRIDGE_TABLE = "bridge";

    /**
     * A key on the services table to expose  an instance of this type to the underlying Lua script.  This allows
     * the underlying script to perform functions such as getting the current path, or posting events to subscribers.
     */
    public static final String THIS_INSTANCE = "resource";

    /**
     * Exposes the instance of {@link IocResolver} which the underlying script can use to resolve dependencies
     * such as other instances of {@link Resource}, {@link EdgeServer}, and {@link InternalServer}.
     */
    public static final String IOC_INSTANCE = "ioc";

    private final LuaState luaState;

    private final IocResolver iocResolver;

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
                               final IocResolver iocResolver) {
        this.luaState = luaState;
        this.iocResolver = iocResolver;
    }

    /**
     * Loads and runs a Lua script from the given {@link InputStream} instance.  The name
     * supplied is useful for debugging and should match the name of the file from which
     * the script was loaded.
     *
     *
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

            // Places a table in the registry to hold the currently running threads.
            luaState.newTable();
            luaState.setField(LuaState.REGISTRYINDEX, SERVER_THREADS_TABLE);

            // Creates a new table to hold the server table functions
            luaState.newTable();

            // Creates a place for server.request
            luaState.newTable();
            luaState.setField(-2, REQUEST_TABLE);

            // Creates a table for server.coroutine.  This houses code for
            // server-managed coroutines that will automatically be activated
            // on every update.

            luaState.newTable();
            luaState.pushJavaFunction(serverStartCoroutine);
            luaState.setField(-2, COROUTINE_CREATE_FUNCTION);
            luaState.setField(-2, COROUTINE_TABLE);

            // Sets up the services table which references htis and the IOC resolver
            // instance.

            luaState.newTable();

            luaState.pushJavaObject(this);
            luaState.setField(-2, THIS_INSTANCE);

            luaState.pushJavaObject(iocResolver);
            luaState.setField(-2, IOC_INSTANCE);

            luaState.setField(-2, BRIDGE_TABLE);

            // Finally sets the server table to be in the global space
            luaState.setGlobal(NAMAZU_RT_TABLE);

            // Lastly we hijack the standard lua print function to redirect
            // to the underlying logging system
            luaState.pushJavaFunction(printToScriptLog);
            luaState.setGlobal(PRINT_FUNCTION);

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
        luaState.close();
    }

}
