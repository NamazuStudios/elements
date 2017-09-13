package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Scheduler;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.StackProtector;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;

import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CoroutineBuiltin implements Builtin {

    public static final String MODULE_NAME = "namazu.coroutine";

    public static final String COROUTINES_TABLE = "NAMAZU_THREADS";

    public static final String START_COROUTINE = "start";

    private final LuaResource luaResource;

    private final Scheduler scheduler;

    public CoroutineBuiltin(final LuaResource luaResource, final Scheduler scheduler) {
        this.luaResource = luaResource;
        this.scheduler = scheduler;
    }

    private final JavaFunction start = luaState ->  {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            if (!luaState.isThread(0)) {
                throw new IllegalArgumentException("namazu.coroutine.start must be supplied a coroutine");
            }

            final TaskId taskId = new TaskId();
            luaState.getField(LuaState.REGISTRYINDEX, COROUTINES_TABLE);
            luaState.pushValue(0);
            luaState.setField(-2, taskId.asString());
            luaState.pop(0);

            final int returned = luaState.resume(0, luaState.getTop() - 1);

            final YieldInstruction instruction =
                returned > 0  && luaState.isJavaObject(0, YieldInstruction.class) ?
                        luaState.checkJavaObject(0, YieldInstruction.class) :
                        YieldInstruction.IMMEDIATE;

            switch (instruction) {
                case FOR:
                    scheduleFor(taskId, luaState);
                    break;
                case UNTIL:
                    scheduleUntil(taskId, luaState);
                    break;
                case IMMEDIATE:
                    scheduleImmediate(taskId);
                    break;
                case UNTIL_NEXT_CRON:
                    scheduleUntilNextCron(taskId, luaState);
                    break;
                default:
                    throw new InternalException("unknown enum value " + instruction);
            }

//            final double timeValue =
//                    returned > 1 && luaState.isNumber( 1) ? luaState.checkNumber(1, 0) :
//                    returned > 1 && luaState.isString( 1) ? parseCron(instruction, luaState.checkString(1)) :
//                                                                   0;
//
//            final TimeUnit timeUnit =
//                    returned > 2 && luaState.isJavaObject(2, TimeUnit.class) ?
//                        luaState.checkJavaObject(2, TimeUnit.class) :
//                        TimeUnit.SECONDS;
//
//            return stackProtector.setAbsoluteIndex(0);

            return 0;

        }
    };

    private void scheduleImmediate(final TaskId taskId) {
        getScheduler().resumeTask(getLuaResource().getId(), taskId);
    }

    private void scheduleUntil(final TaskId taskId, final LuaState luaState) {
        final long delay = delayUntilMilliseconds(luaState);
        getScheduler().resumeTaskAfterDelay(getLuaResource().getId(), delay, MILLISECONDS, taskId);
    }

    private long delayUntilMilliseconds(final LuaState luaState) {
        return 0;
    }

    private void scheduleFor(final TaskId taskId, final LuaState luaState) {
        final long delay = delayForMilliseconds(luaState);
        getScheduler().resumeTaskAfterDelay(getLuaResource().getId(), delay, MILLISECONDS, taskId);
    }

    private long delayForMilliseconds(final LuaState luaState) {
        return 0;
    }

    private void scheduleUntilNextCron(final TaskId taskId, final LuaState luaState) {
        final long delay = delayUntilNextCronMilliseconds(luaState);
        getScheduler().resumeTaskAfterDelay(getLuaResource().getId(), delay, MILLISECONDS, taskId);
    }

    private long delayUntilNextCronMilliseconds(final LuaState luaState) {
        return 0;
    }

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

    public LuaResource getLuaResource() {
        return luaResource;
    }

    public void startCoroutine(final Object[] params,
                               final Consumer<Object> consumer,
                               final Consumer<Throwable> throwableConsumer) {
        // TODO Implement this
    }

}
