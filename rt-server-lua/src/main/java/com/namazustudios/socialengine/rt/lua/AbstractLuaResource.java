package com.namazustudios.socialengine.rt.lua;

import com.google.common.base.Strings;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
    public static final String SERVER_TABLE = "server";

    /**
     * The table underneath the Server table which handles requets.
     */
    public static final String REQUEST_TABLE = "request";

    /**
     * The table underneath the Server table which handles requets.
     */
    public static final String EVENT_TABLE = "event";

    /**
     * Constant to designate the time between concurrent execution of server updates.
     */
    public static final String DELTA_TIME_KEY = "delta_time";

    /**
     * Constant to designate the server.coroutine table.  This is a set of coroutinee
     * functions managed by the server.
     */
    public static final String COROUTINE_TABLE = "coroutine";

    /**
     * The name of the server.coroutine.create() function
     */
    public static final String COROUTINE_CREATE_FUNCTION = "create";

    /**
     * A registery table for server threads.
     */
    public static final String SERVER_THREADS_TABLE = "RT_SERVER_THREADS";

    /**
     * Constant to designate the "Handler" key.  This is used as the
     * key for a lua function which can be called to handle a {@link Request}
     * for a type.
     */
    public static final String RQUEST_HANDLER_KEY = "handler";

    /**
     * Constant to designate the "type" key for event, request, or response.
     * This is optional in many cases, but can be used to resolve ambiguities.
     */
    public static final String JAVA_TYPE = "type";

    /**
     * The lua state used by this and subclasses.
     */
    protected final LuaState luaState;

    @Inject
    private TypeRegistry typeRegistry;

    /**
     * Creates an instance of {@link AbstractLuaResource} with the given {@link LuaState}
     * type.
     *
     * @param luaState the luaState
     */
    @Inject
    public AbstractLuaResource(final LuaState luaState) {

        this.luaState = luaState;

        // Places a table in the registry to hold the currently running threads.
        luaState.newTable();
        luaState.pushValue(-1);
        luaState.setField(LuaState.REGISTRYINDEX, SERVER_THREADS_TABLE);

        // We also mark the table as weak using a metatable with value { "__mode" = "v" }
        luaState.newTable();
        luaState.pushString("__mode");
        luaState.setField(-2, "v");
        luaState.setMetatable(-2);

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
        luaState.setGlobal(SERVER_TABLE);

    }

    @Override
    protected void doUpdate(double deltaTime) {
        try {
            setDetalTime(deltaTime);
            runServerCoroutines(deltaTime);
        } finally {
            clearDeltaTime();
        }
    }

    private void setDetalTime(double deltaTime) {
        luaState.getGlobal(SERVER_TABLE);
        luaState.pushNumber(deltaTime);
        luaState.setField(-2, DELTA_TIME_KEY);
        luaState.pop(1);
    }

    private void runServerCoroutines(double deltaTime) {

        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.getField(LuaState.REGISTRYINDEX, SERVER_THREADS_TABLE);

            final int threadTableIndex = luaState.absIndex(-1);

            luaState.pushNil();
            while (luaState.next(threadTableIndex)) {
                if (luaState.isThread(-1)) {
                    luaState.pushNumber(deltaTime);
                    final int returnCount = luaState.resume(-2, -1);
                    luaState.pop(1 + returnCount);
                }
            }
        }

    }

    private void clearDeltaTime() {
        luaState.getGlobal(SERVER_TABLE);
        luaState.pushNil();
        luaState.setField(-2, DELTA_TIME_KEY);
        luaState.pop(1);
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

            luaState.getGlobal(SERVER_TABLE);
            luaState.getField(-1, EVENT_TABLE);

            final int eventTable = luaState.absIndex(-1);

            luaState.pushNil();
            while (luaState.next(eventTable)) {

                final String eventName = Strings.nullToEmpty(luaState.checkString(-2)).trim();
                final String eventType = luaState.isString(-1) ? Strings.nullToEmpty(luaState.checkString(-1)).trim() :
                                                                 null;

                if (eventName.equals(desiredName)) {
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
