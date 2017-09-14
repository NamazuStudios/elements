package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Scheduler;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.cronutils.model.time.ExecutionTime.forCron;
import static com.naef.jnlua.LuaState.REGISTRYINDEX;
import static com.naef.jnlua.LuaState.YIELD;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.YieldInstruction.IMMEDIATE;
import static java.lang.Math.max;
import static java.lang.StrictMath.round;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CoroutineBuiltin implements Builtin {

    private static final long TIME_UNIT_CORRECTION_FACTOR_L = 10000;

    private static final double TIME_UNIT_CORRECTION_FACTOR_D = TIME_UNIT_CORRECTION_FACTOR_L;

    public static final String MODULE_NAME = "namazu.coroutine";

    public static final String COROUTINES_TABLE = "NAMAZU_THREADS";

    public static final String START = "start";

    public static final String RESUME = "resume";

    public static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J);

    private final LuaResource luaResource;

    private final Scheduler scheduler;

    public CoroutineBuiltin(final LuaResource luaResource, final Scheduler scheduler) {
        this.luaResource = luaResource;
        this.scheduler = scheduler;
    }

    /**
     * Accepts a coroutine along with arguments to pass to it.  This will run the coroutine, accepting yield
     * instructions until the coroutine has completed.  The server will manage the running task until the coroutine
     * finishes, or throws an exception.
     *
     * Any time the task is updated, an exception may be thrown.
     */
    private final JavaFunction start = luaState ->  {

        if (!luaState.isThread(1)) {
            throw new IllegalArgumentException("namazu.coroutine.start must be supplied a coroutine");
        }

        final TaskId taskId = new TaskId();

        luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
        luaState.pushValue(1);
        luaState.setField(-2, taskId.asString());
        luaState.pop(1);

        return resume(taskId, luaState) + 1;

    };

    private final JavaFunction resume = luaState ->  {

        // Calculate the task id from the first argument passed in.  Instead of accepting a coroutine
        // we must accept the ID of the task, so we can look up the coroutine that's associated with the
        // specified task ID.

        final TaskId taskId = new TaskId(luaState.checkString(0));
        luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
        luaState.getField(-1, taskId.toString());

        if (!luaState.isThread(-1)) {
            throw new InternalException("no such task: " + taskId);
        }

        luaState.pushValue(1);
        luaState.replace(0);
        luaState.pop(1);

        return resume(taskId, luaState) + 1;

    };

    private int resume(final TaskId taskId, final LuaState luaState) {

        final int returned = luaState.resume(0, luaState.getTop() - 1);

        luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
        luaState.getField(-1, taskId.toString());

        final int status = luaState.status(-1);
        luaState.pop(2);

        if (status == YIELD) {

            // If we yielded, then we start looking for the yield instructions.  If the yield instructions fail at any
            // point then we simply throw an exception.  If we did not yield, we simply capture the return values and
            // leave the return values on the stack.  We catch any yielding values and we simply process them and
            // wipe the stack clean.

            processYieldInstruction(taskId, luaState);
            luaState.setTop(0);
            return 0;
        } else {

            // If the coroutine wasn't yielded because it finished normally, then we simply take what's on the stack
            // and return it to the caller.  The caller then will collect the values that are returned.  Of course
            // any exceptions shoudl be caught.

            return returned;

        }

    }

    private void processYieldInstruction(final TaskId taskId,
                                         final LuaState luaState) {

        final YieldInstruction instruction;
        instruction = luaState.getTop() > 1 ? luaState.checkJavaObject(0, YieldInstruction.class) : IMMEDIATE;

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
            case UNTIL_NEXT:
                scheduleUntilNextCron(taskId, luaState);
                break;
            default:
                throw new InternalException("unknown enum value " + instruction);
        }

    }

    private void scheduleImmediate(final TaskId taskId) {
        getScheduler().resumeTask(getLuaResource().getId(), taskId);
    }

    private void scheduleUntil(final TaskId taskId, final LuaState luaState) {
        final long delay = delayUntilMilliseconds(luaState);
        getScheduler().resumeTaskAfterDelay(getLuaResource().getId(), delay, MILLISECONDS, taskId);
    }

    private long delayUntilMilliseconds(final LuaState luaState) {
        final long now = currentTimeMillis();
        final long value = timeValueInMilliseconds(luaState);
        return max(0, value - now);
    }

    private void scheduleFor(final TaskId taskId, final LuaState luaState) {
        final long delay = timeValueInMilliseconds(luaState);
        getScheduler().resumeTaskAfterDelay(getLuaResource().getId(), delay, MILLISECONDS, taskId);
    }

    private long timeValueInMilliseconds(final LuaState luaState) {

        if (luaState.getTop() < 2) {
            throw new IllegalArgumentException("time value must be specified");
        }

        final TimeUnit timeUnit = timeUnit(luaState);
        final double value = luaState.checkNumber(1) * TIME_UNIT_CORRECTION_FACTOR_D;

        return MILLISECONDS.convert(round(value), timeUnit) / TIME_UNIT_CORRECTION_FACTOR_L;

    }

    private TimeUnit timeUnit(final LuaState luaState) {
        return luaState.getTop() > 2 ? luaState.checkJavaObject(2, TimeUnit.class) : TimeUnit.SECONDS;
    }

    private void scheduleUntilNextCron(final TaskId taskId, final LuaState luaState) {
        final long delay = delayUntilNextCronMilliseconds(luaState);
        getScheduler().resumeTaskAfterDelay(getLuaResource().getId(), delay, MILLISECONDS, taskId);
    }

    private long delayUntilNextCronMilliseconds(final LuaState luaState) {

        if (luaState.getTop() < 2) {
            throw new IllegalArgumentException("time value must be specified");
        }

        final String expression = luaState.checkString(1);
        final CronParser cronParser = new CronParser(CRON_DEFINITION);
        final Cron cron = cronParser.parse(expression);

        final ExecutionTime executionTime = forCron(cron);
        final Duration duration = executionTime.timeToNextExecution(ZonedDateTime.now()).get();

        return duration.get(ChronoUnit.MILLIS);

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
