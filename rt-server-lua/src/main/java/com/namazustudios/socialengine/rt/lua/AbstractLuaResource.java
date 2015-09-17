package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.Converter;
import com.naef.jnlua.JavaFunction;
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
     * The name of a global table where the Lua code interacts with the
     * underlying server APIs.
     */
    public static final String NAMAZU_TABLE = "namazu_rt";

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
     * Constant to designate the "Handler" key.  This is used as the key for a lua function which can be called to
     * handle a {@link Request}.  This is necessary, or else no functionality in Lua will exist to handle
     * the request and must be assigned to a function type.
     */
    public static final String REQUEST_HANDLER_KEY = "handler";

    /**
     * Constant to designate the {@link Class} of the the request.  If set to a string other than
     * {@link #WILDCARD_TYPE_TOKEN} this will attempt to force type conversion to that particular type.
     * Leaving blank will fall back onto the behavior of {@link Converter#convertJavaObject(LuaState, Object)}.
     * Specifying type should rarely be needed.
     *
     * More specifically this controls the return value of {@link EdgeRequestPathHandler#getPayloadType()} to
     * tell upstream code how to deserialize objects transmitted over the network.
     */
    public static final String REQUEST_PAYLOAD_JAVA_TYPE = "request_payload_type";

    /**
     * The wildcard type.  This is {@link Map}, which translates to a lua table.
     */
    public static final Class<?> WILDCARD_TYPE = Map.class;

    /**
     * Used by the type functions to indicate a type wildcard.
     */
    public static final String WILDCARD_TYPE_TOKEN = "*";

    /**
     * The wildcard type.  This is {@link List}, which translates to a lua table with
     * numeric indices.
     */
    public static final Class<?> WILDCARD_ARRAY_TYPE = List.class;

    /**
     * Used by the type functions to indicate a type wildcard.
     */
    public static final String WILDCARD_ARRAY_TYPE_TOKEN = "{*}";

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

    private final TypeRegistry typeRegistry;

    private final IocResolver iocResolver;

    private Logger scriptLog = LOG;

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
                return stackProtector.ret(1);


            }
        }
    };

    /**
     * Creates an instance of {@link AbstractLuaResource} with the given {@link LuaState}
     * type.
     *
     * If instantion fails, it is the responsilbity of the caller to deallocate the {@link LuaState}
     * object.  If the constructor completes without error, then the caller does not need to close
     * the state as it will be handled by this object's {@link #close()} method.
     *
     * @param luaState the luaState
     */
    public AbstractLuaResource(final LuaState luaState,
                               final IocResolver iocResolver,
                               final TypeRegistry typeRegistry) {
        this.luaState = luaState;
        this.iocResolver = iocResolver;
        this.typeRegistry = typeRegistry;
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

        scriptLog = LoggerFactory.getLogger(name);
        setupScriptGlobals();

        luaState.load(inputStream, name, "bt");
        getScriptLog().debug("Loaded lua script.", luaState);

        luaState.call(0, 0);
        getScriptLog().debug("Executed lua script.", luaState);

    }

    private void setupScriptGlobals() {

        // Places a table in the registry to hold the currently running threads.
        luaState.newTable();
        luaState.pushValue(-1);
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
        luaState.setGlobal(NAMAZU_TABLE);

    }

    @Override
    protected void doUpdate(double deltaTime) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            runServerCoroutines(deltaTime);
        }
    }

    private void runServerCoroutines(double deltaTime) {

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

                if ((threadStatus != LuaState.YIELD) || (threadStatus != luaState.OK)) {
                    luaState.pop(1);
                    threadsToReap.add(luaState.checkString(-2));
                    continue;
                }

                try (final StackProtector threadStackProtector = new StackProtector(luaState)){
                    luaState.pushNumber(deltaTime);
                    final int returnCount = luaState.resume(-2, 1);
                    luaState.pop(returnCount);
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
     *
     * Pushes the given request handler on the stack for the given name and type.  If the type is not
     * found, then this will throw a {@link NotFoundException} and restore the stack to the original
     * state.
     *
     * @param methodName desired method name
     *
     * @return the request handler's desired type, which may be the default type ({@link Map})
     *
     */
    protected Class<?> getRequestType(final String methodName) {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            pushRequestHandlerTable(methodName);

            // Before pushing the handler, we have to finally check the payload and
            // the response type.  Note that the script can leave these blank and
            // it will fall back onto the default wildcard type.

            luaState.getField(-1, REQUEST_PAYLOAD_JAVA_TYPE);
            final String requestPayloadTypeName = luaState.checkString(-1, WILDCARD_TYPE_TOKEN);
            luaState.pop(1);

            switch (requestPayloadTypeName) {
                case WILDCARD_TYPE_TOKEN:
                    return WILDCARD_TYPE;
                case WILDCARD_ARRAY_TYPE_TOKEN:
                    return WILDCARD_ARRAY_TYPE;
                default:
                    return typeRegistry.getRequestPayloadTypeNamed(requestPayloadTypeName);
            }

        }

    }

    /**
     * Pushes the handler request function on the top of the stack.
     *
     * Any other intermediate variables are popped on the stack.  The end result of this
     * call should result in only the requets handler table being pushed.
     *
     * @param methodName the method name
     */
    protected void pushRequestHandlerFunction(final String methodName) {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            pushRequestHandlerTable(methodName);

            luaState.getField(-1, REQUEST_HANDLER_KEY);

            if (!luaState.isFunction(-1)) {
                getScriptLog().error("No handler function found at {}.{}.{}.{}",
                                     NAMAZU_TABLE, REQUEST_TABLE, methodName, REQUEST_HANDLER_KEY);
                throw new NotFoundException("request handler not found for method " + methodName);
            }

            luaState.pop(-2);
            stackProtector.ret(1);

        }

    }

    /**
     * Pushes the request handler table for the given method name.  The request handler
     * table can contain two keys of use.  The handler function itself, and the method
     * type expected for the request.
     *
     * Any other intermediate variables are popped on the stack.  The end result of this
     * call should result in only the requets handler table being pushed.
     *
     * @param methodName the method name
     *
     * @throws {@link NotFoundException} if methodName name is not found
     *
     */
    protected void pushRequestHandlerTable(final String methodName) {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            luaState.getGlobal(NAMAZU_TABLE);

            // The first two checks shouldn't fail, unless somebody has seriously
            // hosed the lua script backing this resource.

            if (!luaState.isTable(-1)) {
                getScriptLog().error("Unable to find table {}", NAMAZU_TABLE);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            luaState.getField(-1, REQUEST_TABLE);
            luaState.remove(-2);  // pops namazu_rt

            if (!luaState.isTable(-1)) {
                getScriptLog().error("Unable to find table {}.{}", NAMAZU_TABLE, REQUEST_TABLE);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            // Here's where the failures can be considered "normal" in that somebody could
            // have just forgotten to define a handler.

            luaState.getField(-1, methodName);
            luaState.remove(-2); // pops request

            if (!luaState.isTable(-1)) {
                getScriptLog().warn("Unable to find table {}.{}.{}", NAMAZU_TABLE, REQUEST_TABLE, methodName);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            stackProtector.ret(1);

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

        if (getScriptLog().isErrorEnabled()) {

            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Lua Stack: \n");

            for (int i = 1; i <= luaState.getTop(); ++i) {
                stringBuilder.append("  Element ")
                             .append(i).append(" ")
                             .append(luaState.type(i)).append(" ")
                             .append(luaState.toJavaObjectRaw(i))
                             .append('\n');
            }

            getScriptLog().error("{}", stringBuilder);

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
