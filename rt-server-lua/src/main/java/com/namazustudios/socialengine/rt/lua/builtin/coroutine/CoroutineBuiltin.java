package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Scheduler;
import com.namazustudios.socialengine.rt.lua.StackProtector;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;

import javax.inject.Inject;
import java.util.UUID;

public class CoroutineBuiltin implements Builtin {

    public static final String MODULE_NAME = "namazu.coroutine";

    public static final String THREADS_TABLE = "NAMAZU_THREADS";

    public static final String START_COROUTINE = "start";

    private Scheduler scheduler;

    private final JavaFunction start = luaState ->  {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            if (!luaState.isFunction(-1)) {
                throw new IllegalArgumentException("scheduler.coroutine.create() must be passed a function");
            }

            final UUID uuid = UUID.randomUUID();

            luaState.newThread();
            luaState.getField(LuaState.REGISTRYINDEX, THREADS_TABLE);
            luaState.pushValue(-2);
            luaState.setField(-2, uuid.toString());
            luaState.pop(1);

            return stackProtector.setAbsoluteIndex(1);

        }
    };

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {
            @Override
            public String getChunkName() {
                return MODULE_NAME;
            }

            @Override
            public boolean exists() {
                return MODULE_NAME.equals(moduleName);
            }
        };
    }

    @Override
    public JavaFunction getLoader() {
        return null;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private void schedule(final String coroutine) {

    }

}
