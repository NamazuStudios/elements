package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Server;

import java.util.*;

/**
 * A simple class which manages the coroutines for the {@link AbstractLuaResource} instances.
 *
 * Created by patricktwohig on 11/2/15.
 */
public class CoroutineManager {

    private final AbstractLuaResource abstractLuaResource;

    private final Server server;

    private final SortedMap<Double, String> timerMap = new TreeMap<>();

    /**
     * Creates a new thread managed by the server.  This returns to the calling code
     * the thread that was created.
     */
    private final JavaFunction serverStartCoroutine = new JavaFunction() {
        @Override
        public int invoke(final LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                if (!luaState.isFunction(-1)) {
                    abstractLuaResource.dumpStack();
                    throw new IllegalArgumentException("server.coroutine.create() must be passed a function");
                }

                final UUID uuid = UUID.randomUUID();

                luaState.newThread();
                luaState.getField(LuaState.REGISTRYINDEX, Constants.SERVER_THREADS_TABLE);
                luaState.pushValue(-2);
                luaState.setField(-2, uuid.toString());
                luaState.pop(1);

                abstractLuaResource.getScriptLog().info("Created coroutine {}", uuid);
                return stackProtector.setAbsoluteIndex(1);

            }
        }
    };

    public CoroutineManager(final AbstractLuaResource abstractLuaResource, final Server server) {
        this.abstractLuaResource = abstractLuaResource;
        this.server = server;
    }

    public void setup() {

        // Creates a table for server.coroutine.  This houses code for
        // server-managed coroutines that will automatically be activated
        // on every update.
        final LuaState luaState = abstractLuaResource.getLuaState();

        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.getGlobal(Constants.NAMAZU_RT_TABLE);
            luaState.newTable();
            luaState.pushJavaFunction(serverStartCoroutine);
            luaState.setField(-2, Constants.COROUTINE_CREATE_FUNCTION);
            luaState.setField(-2, Constants.COROUTINE_TABLE);
            luaState.pop(1);
        }

    }

    public void runManagedCoroutines(double deltaTime) {

        try (final StackProtector stackProtector = new StackProtector(abstractLuaResource.getLuaState())) {

            final LuaState luaState = abstractLuaResource.getLuaState();

            luaState.getField(LuaState.REGISTRYINDEX, Constants.SERVER_THREADS_TABLE);

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
                        abstractLuaResource.dumpStack();
                        abstractLuaResource.dumpStack(ex);
                    }
                } else {
                    threadsToReap.add(luaState.checkString(-2));
                }

                luaState.pop(1);

            }

            for (final String uuid : threadsToReap) {
                abstractLuaResource.getScriptLog().info("Reaping thread {}.", uuid);
                luaState.pushNil();
                luaState.setField(threadTableIndex, uuid);
            }

            luaState.pop(1);

        }

    }

}
