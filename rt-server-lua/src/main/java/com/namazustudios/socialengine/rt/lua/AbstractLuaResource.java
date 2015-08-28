package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.Converter;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.AbstractResource;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import com.namazustudios.socialengine.rt.internal.InternalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by patricktwohig on 8/25/15.
 */
public class AbstractLuaResource extends AbstractResource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLuaResource.class);

    private static final JavaFunction SERVER_START_COROUTINE = new JavaFunction() {
        @Override
        public int invoke(LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                if (luaState.isFunction(-1)) {
                    throw new IllegalArgumentException("server.coroutine.create() must be passed a function");
                }

                final UUID uuid = UUID.randomUUID();
                LOG.info("Starting coroutine {}", uuid);
                luaState.newThread();
                luaState.pushValue(-1);
                luaState.setField(LuaState.REGISTRYINDEX, uuid.toString());

                return stackProtector.ret(1);

            }
        }
    };

    /**
     * The name of a global table where the Lua code interacts with the
     * underlying server APIs.
     */
    public static final String NAMAZU_TABLE = "namazu_rt";

    /**
     * The table underneath the namazu_rt table which specifies which events this script
     * will source.  Typically this is assigned to nil or the {@link #WILDCARD_TYPE_TOKEN} constant
     * to indicate that the event will match any type.
     */
    public static final String EVENT_TABLE = "event";

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
     * Constant to designate the "type" key for the response.  If set to a string other than {@link #WILDCARD_TYPE_TOKEN}
     * this will attempt to force type conversion to that particular type.  Leaving blank will fall back onto
     * the behavior of {@link Converter#convertJavaObject(LuaState, Object)}.  Specifying type should rarely
     * be needed.
     *
     * More specifically this controls the return value of {@link EdgeRequestPathHandler#getPayloadType()} and
     * similar functions.
     */
    public static final String REQUEST_PAYLOAD_JAVA_TYPE = "request_payload_type";

    /**
     * The wildcard type.
     */
    public static final Class<?> WILDCARD_TYPE = Map.class;

    /**
     * A key on the namazu_rt table to expose  an instance of this type to the underlying Lua script.  This allows
     * the underlying script to perform functions such as getting the current path, or posting events to subscribers.
     */
    public static final String THIS_INSTANCE = "resource";

    /**
     * Exposes the instance of {@link IocResolver} which the underlying script can use to resolve depdendencies
     * such as other instances of {@link Resource}, {@link EdgeServer}, and {@link InternalServer}.
     */
    public static final String IOC_INSTNCE = "ioc";

    /**
     * Used by the type functions to indicate a type wildcard.
     */
    public static final String WILDCARD_TYPE_TOKEN = "*";


    /**
     * The lua state used by this and subclasses.  This is injected by the IoC container
     * configured with the possible options such as an application-specific {@link Converter} instance.
     */
    private final LuaState luaState;

    private final IocResolver iocResolver;

    private final TypeRegistry typeRegistry;

    /**
     * Creates an instance of {@link AbstractLuaResource} with the given {@link LuaState}
     * type.
     *
     * @param luaState the luaState
     */
    public AbstractLuaResource(final LuaState luaState,
                               final IocResolver iocResolver,
                               final TypeRegistry typeRegistry) {

        this.luaState = luaState;
        this.iocResolver = iocResolver;
        this.typeRegistry = typeRegistry;

        // Places a table in the registry to hold the currently running threads.
        luaState.newTable();
        luaState.pushValue(-1);
        luaState.setField(LuaState.REGISTRYINDEX, SERVER_THREADS_TABLE);

        // Creates a new table to hold the server table functions
        luaState.newTable();

        // Creates a place for server.request
        luaState.newTable();
        luaState.setField(-2, REQUEST_TABLE);

        // Creates a place for server.event
        luaState.newTable();
        luaState.setField(-2, EVENT_TABLE);

        // Creates a table for server.coroutine.  This houses code for
        // server-managed coroutines that will automatically be activated
        // on every update.

        luaState.newTable();
        luaState.pushJavaFunction(SERVER_START_COROUTINE);
        luaState.setField(-2, COROUTINE_CREATE_FUNCTION);
        luaState.setField(-2, COROUTINE_TABLE);

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
                    threadsToReap.add(luaState.checkString(-2));
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
            // the response type.  Note that the script can leave these blank

            luaState.getField(-1, REQUEST_PAYLOAD_JAVA_TYPE);
            final String requestPayloadTypeName = luaState.checkString(-1, WILDCARD_TYPE_TOKEN);
            luaState.pop(1);

            return WILDCARD_TYPE_TOKEN.equals(requestPayloadTypeName) ?
                WILDCARD_TYPE :
                typeRegistry.getRequestPayloadTypeNamed(requestPayloadTypeName);

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
                LOG.error("No handler function found at {}.{}.{}.{}",
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

            // The first two checks shouldn't fail, unless soembody has seriously
            // hosed the lua script backing this resource.

            if (!luaState.isTable(-1)) {
                LOG.error("Unable to find table {}.{}", REQUEST_TABLE, NAMAZU_TABLE);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            luaState.getField(-1, REQUEST_TABLE);
            luaState.remove(-2);  // pops namazu_rt

            if (!luaState.isTable(-1)) {
                LOG.error("Unable to find table {}.{}", REQUEST_TABLE, NAMAZU_TABLE);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            // Here's where the failures can be considered "normal" in that somebody could
            // have just forgotten to define a handler.

            luaState.getField(-1, methodName);
            luaState.remove(-2); // pops request

            if (!luaState.isTable(-1)) {
                LOG.warn("Unable to find {} in {}.{}", methodName, NAMAZU_TABLE, REQUEST_TABLE);
                throw new NotFoundException(methodName + " doest not exist for " + this);
            }

            stackProtector.ret(1);

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
