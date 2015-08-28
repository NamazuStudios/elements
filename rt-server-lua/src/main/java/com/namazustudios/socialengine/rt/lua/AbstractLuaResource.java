package com.namazustudios.socialengine.rt.lua;

import com.google.common.base.Strings;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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
     * The table underneath the Server table which handles requets.
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
     * The table underneath the namazu table which handles requests.
     */
    public static final String REQUEST_TABLE = "request";

    /**
     * Constant to designate the "Handler" key.  This is used as the
     * key for a lua function which can be called to handle a {@link Request}
     * for a type.
     */
    public static final String REQUEST_HANDLER_KEY = "handler";

    /**
     * Constant to designate the "type" key for the response. This is optional in many cases,
     * but can be used to resolve ambiguities.  If left nil, then type conversion will be
     * performed through the {@link com.naef.jnlua.Converter} interface.
     */
    public static final String REQUEST_PAYLOAD_JAVA_TYPE = "request_payload_type";

    /**
     * Constant to designate the "type" key for the response. This is optional in many cases,
     * but can be used to resolve ambiguities.  If left nil, then type conversion will be
     * performed through the {@link com.naef.jnlua.Converter} interface.
     */
    public static final String RESPONSE_PAYLOAD_JAVA_TYPE = "response_payload_type";

    /**
     * Used by the type functions to indicate a type wildcard.
     */
    public static final String TYPE_WILDCARD = "*";

    /**
     * The lua state used by this and subclasses.  This is injected by the IoC container
     * int the
     */
    protected final LuaState luaState;

    @Inject
    IocResolver iocResolver;

    @Inject
    private TypeRegistry typeRegistry;

    /**
     * Creates an instance of {@link AbstractLuaResource} with the given {@link LuaState}
     * type.
     *
     * @param luaState the luaState
     */
    @Inject
    public AbstractLuaResource(final LuaState luaState,
                               final IocResolver iocResolver) {

        this.luaState = luaState;

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
     * In addition to checking the base type, this will check the Lua event table
     * for both the type and the value.
     *
     * @see {@link TypeRegistry#getEventTypeNamed(String)}
     * @see {@link AbstractLuaResource#checkEvents(String, Class)}
     *
     * @param desiredName the name of the event
     * @param desiredType the desired type of the event, as obtained from {@link EventReceiver#getEventType()}.
     *
     * @return true if the underlying lua script supports the event
     */
    @Override
    public boolean checkEvents(final String desiredName, final Class<?> desiredType) {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            boolean found = false;

            luaState.getGlobal(NAMAZU_TABLE);
            luaState.getField(-1, EVENT_TABLE);

            final int eventTable = luaState.absIndex(-1);

            luaState.pushNil();
            while (luaState.next(eventTable)) {

                final String eventName = Strings.nullToEmpty(luaState.checkString(-2)).trim();
                final String eventType = luaState.checkString(-1, TYPE_WILDCARD);

                // Skip the check if somebody sets to the wildcard type

                if (!TYPE_WILDCARD.equals(eventType) && eventName.equals(desiredName)) {

                    if (eventType != null) {

                        final Class<?> eventClass = typeRegistry.getEventTypeNamed(luaState.checkString(-1));

                        if (!desiredType.isAssignableFrom(eventClass)) {
                            throw new NotFoundException("event handler for type " + desiredType + " not found.");
                        }

                    }

                    found = true;

                }

                luaState.pop(1);

            }

            luaState.pop(1);

            return found || super.checkEvents(desiredName, desiredType);

        }
    }

    /**
     *
     * Pushes the given request handler on the stack for the given name and type.  If the type is not
     * found, then this will throw a {@link NotFoundException} and restore the stack to the original
     * state.
     *
     * @param desiredRequestName desired request name
     * @param desiredRequestPayloadType the desired request type
     * @param desiredResponsePayloadType the desired payload type
     *
     */
    public void pushRequestHandler(final String desiredRequestName,
                                   final Class<?> desiredRequestPayloadType,
                                   final Class<?> desiredResponsePayloadType) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            luaState.getGlobal(NAMAZU_TABLE);

            // The first two checks shouldn't fail, unless soembody has seriously
            // hosed the lua script backing this resource.

            if (!luaState.isTable(-1)) {
                LOG.error("Unable to find table {}.{}", REQUEST_TABLE, NAMAZU_TABLE);
                throw new NotFoundException(desiredRequestName + " doest not exist for " + this);
            }

            luaState.getField(-1, REQUEST_TABLE);

            if (!luaState.isTable(-1)) {
                LOG.error("Unable to find table {}.{}", REQUEST_TABLE, NAMAZU_TABLE);
                throw new NotFoundException(desiredRequestName + " doest not exist for " + this);
            }

            // Here's where the failures can be considered "normal" in that somebody could
            // have just forgotten to define a handler.

            luaState.getField(-1, desiredRequestName);

            if (!luaState.isTable(-1)) {
                LOG.warn("Unable to find {} in {}.{}", desiredRequestName, NAMAZU_TABLE, REQUEST_TABLE);
                throw new NotFoundException(desiredRequestName + " doest not exist for " + this);
            }

            // Before pushing the handler, we have to finally check the payload and
            // the response type.  Note that the script can leave these blank

            luaState.getField(-1, REQUEST_PAYLOAD_JAVA_TYPE);
            final String requestPayloadTypeName = luaState.checkString(-1, TYPE_WILDCARD);
            luaState.pop(1);

            if (!TYPE_WILDCARD.equals(requestPayloadTypeName)) {

                final Class<?> requestPayloadType = typeRegistry.getRequestPayloadTypeNamed(requestPayloadTypeName);

                if (!desiredRequestPayloadType.isAssignableFrom(requestPayloadType)) {
                    throw new NotFoundException("Handler types incompatible  " + requestPayloadType + " " +
                                                "and " + desiredRequestPayloadType);
                }

            }

            luaState.getField(-1, RESPONSE_PAYLOAD_JAVA_TYPE);
            final String responsePayloadTypeName = luaState.checkString(-1, TYPE_WILDCARD);
            luaState.pop(1);

            if (!TYPE_WILDCARD.equals(responsePayloadTypeName)) {

                final Class<?> responsePayloadType = typeRegistry.getResponsePayloadTypeNamed(responsePayloadTypeName);

                if (!desiredResponsePayloadType.isAssignableFrom(responsePayloadType)) {
                    throw new NotFoundException("Handler types incompatible " + responsePayloadType + " " +
                                                "and " + desiredResponsePayloadType);
                }

            }

            // Last step is to actually push the handler function.

            luaState.getField(-1, REQUEST_HANDLER_KEY);

            if (!luaState.isFunction(-1)) {
                LOG.error("No handler function found at {}.{}.{}.{}",
                          NAMAZU_TABLE, REQUEST_TABLE, desiredRequestName, REQUEST_HANDLER_KEY);
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
