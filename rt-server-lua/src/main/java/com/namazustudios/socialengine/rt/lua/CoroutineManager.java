package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Scheduler;

import java.util.*;

/**
 * A simple class which manages the coroutines for the {@link AbstractLuaResource} instances.
 *
 * Created by patricktwohig on 11/2/15.
 */
public class CoroutineManager {

    private final AbstractLuaResource abstractLuaResource;

    private final Scheduler scheduler;

    private final SortedMap<Double, UUID> timerMap = new TreeMap<>();

    /**
     * Creates a new thread managed by the scheduler.  This returns to the calling code
     * the thread that was created.
     */
    private final JavaFunction serverStartCoroutine = new JavaFunction() {
        @Override
        public int invoke(final LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                if (!luaState.isFunction(-1)) {
                    abstractLuaResource.dumpStack();
                    throw new IllegalArgumentException("scheduler.coroutine.create() must be passed a function");
                }

                final UUID uuid = UUID.randomUUID();

                luaState.newThread();
                luaState.getField(LuaState.REGISTRYINDEX, Constants.SERVER_THREADS_TABLE);
                luaState.pushValue(-2);
                luaState.setField(-2, uuid.toString());
                luaState.pop(1);
                timerMap.put(0.0, uuid);

                abstractLuaResource.getScriptLog().info("Created coroutine {}", uuid);
                return stackProtector.setAbsoluteIndex(1);

            }
        }
    };

    public CoroutineManager(final AbstractLuaResource abstractLuaResource, final Scheduler scheduler) {
        this.abstractLuaResource = abstractLuaResource;
        this.scheduler = scheduler;
    }

    public void setup() {

        // Creates a table for scheduler.coroutine.  This houses code for
        // scheduler-managed coroutines that will automatically be activated
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

//    public void runManagedCoroutines(double deltaTime) {
//
//        // Slices the map for coroutines which actually need to be run.  That is, all which
//        // are due to resume within the given time.
//
//        final double serverTime = scheduler.getServerTime();
//
//        // Takes a copy of the UUIDs we want ot run
//        final SortedMap<Double, UUID> toRun = timerMap.headMap(serverTime);
//        final List<UUID> toRunUUIDList = new ArrayList<>(toRun.values());
//
//        final LuaState luaState = abstractLuaResource.getLuaState();
//        try (final StackProtector stackProtector = new StackProtector(luaState)) {
//
//            luaState.getField(LuaState.REGISTRYINDEX, Constants.SERVER_THREADS_TABLE);
//
//            // Iterates each coroutine that is due to run, and resumes it.  If the coroutine
//            // returns a value, then the value is used to determine when to resume it.
//
//            for (final UUID uuid : toRunUUIDList) {
//
//                boolean reap = true;
//                luaState.getField(-1, uuid.toString());
//
//                if (luaState.isThread(-1)) {
//
//                    if ((luaState.status(-1) == LuaState.YIELD) || (luaState.status(-1) == luaState.OK)) {
//
//                        // Runs the coroutine, and then schedules it to run again later.
//                        final double sleepTime = runCoroutine(deltaTime);
//
//                        if (luaState.status(-1) == LuaState.YIELD) {
//                            reap = false;
//                            timerMap.put(serverTime + sleepTime, uuid);
//                        }
//
//                    }
//                }
//
//                // Pops the coroutine itself, leaving the table on the stack
//                luaState.pop(1);
//
//                if (reap) {
//                    abstractLuaResource.getScriptLog().info("Reaping thread {}.", uuid);
//                    luaState.pushNil();
//                    luaState.setField(-2, uuid.toString());
//                }
//
//            }
//
//            luaState.pop(1);
//
//        }
//
//        toRun.clear();
//
//    }

    private double runCoroutine(final double deltaTime) {

        double sleepTime = 0.0;
        final LuaState luaState = abstractLuaResource.getLuaState();

        luaState.pushNumber(deltaTime);

        try {

            final int returnCount = luaState.resume(-2, 1);

            if (returnCount > 0 && luaState.isNumber(-1)) {
                sleepTime = luaState.checkNumber(-1);
            }

            luaState.pop(returnCount);

        } catch (LuaRuntimeException ex) {
            abstractLuaResource.dumpStack(ex);
        }

        return sleepTime;

    }


}
