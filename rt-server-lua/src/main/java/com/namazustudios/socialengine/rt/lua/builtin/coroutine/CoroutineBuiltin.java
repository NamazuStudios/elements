package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;
import com.namazustudios.socialengine.rt.Scheduler;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.concurrent.TimeUnit;

import static com.cronutils.model.time.ExecutionTime.forCron;
import static com.naef.jnlua.LuaState.REGISTRYINDEX;
import static com.naef.jnlua.LuaState.YIELD;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.YieldInstruction.IMMEDIATE;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.StrictMath.round;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CoroutineBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(CoroutineBuiltin.class);

    private static final long TIME_UNIT_CORRECTION_FACTOR_L = 10000;

    private static final double TIME_UNIT_CORRECTION_FACTOR_D = TIME_UNIT_CORRECTION_FACTOR_L;

    public static final String MODULE_NAME = "namazu.coroutine";

    public static final String COROUTINES_TABLE = "namazu.threads";

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

        final LogAssist logAssist = new LogAssist(getLuaResource()::getScriptLog, () -> luaState);

        try {

            luaState.checkType(1, LuaType.THREAD);

            final TaskId taskId = new TaskId();

            luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
            luaState.pushValue(1);
            luaState.setField(-2, taskId.asString());
            luaState.pop(1);

            return resume(taskId, luaState, logAssist);

        } catch (Throwable th) {
            logAssist.error("Could not start coroutine.", th);
            throw th;
        }

    };

    private final JavaFunction resume = luaState ->  {

        final LogAssist logAssist = new LogAssist(getLuaResource()::getScriptLog, () -> luaState);

        try {

            // Calculate the task id from the first argument passed in.  Instead of accepting a coroutine
            // we must accept the ID of the task, so we can look up the coroutine that's associated with the
            // specified task ID.

            final TaskId taskId = new TaskId(luaState.checkString(1));
            luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
            luaState.getField(-1, taskId.toString());

            if (!luaState.isThread(-1)) {
                throw new InternalException("no such task " + taskId + " instead got " + luaState.typeName(-1));
            }

            luaState.replace(1);  // Pops the thread
            luaState.pop(1);      // Replaces the first index

            return resume(taskId, luaState, logAssist);

        } catch (Throwable th) {
            logAssist.error("Could not start coroutine.", th);
            throw th;
        }

    };

    private int resume(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {

        try {

            // Execute the coroutine/thread.  Remember, if the thread is not in the right state, this may cause
            // the thread to fail.  This shouldn't happen because it should be deregistered.

            final int returned = luaState.resume(1, luaState.getTop() - 1);

            // Check the status of the coroutine.  If it is a yield, then we process the yield instructions which will
            // reschedule the task if necessary.  If there's a successful completion, then we collect the results
            // of the method and push them on the stack.

            final int status = luaState.status(1);

            if (status == YIELD) {

                // If we yielded, then we start looking for the yield instructions and process them.  If the yield
                // instructions weren't passed properly, an exception will result.

                processYieldInstruction(taskId, luaState, logAssist);

                // Now that all instructions are processed, we return the status and the task id.

                luaState.setTop(0);
                luaState.pushString(taskId.toString());
                luaState.pushInteger(status);

                return 2;

            } else {

                luaState.remove(1);
                cleanup(taskId, luaState);

                // If the coroutine wasn't yielded because it finished normally, then we simply take what's on the stack
                // and return it to the caller.  The caller then will collect the values that are returned.  However,
                // we do prepend the task ID and status so the caller can make sense of the execution result.

                luaState.pushInteger(status);
                luaState.insert(1);

                luaState.pushString(taskId.toString());
                luaState.insert(1);

                return returned + 2;

            }

        } catch (Throwable th) {
            // All exceptions will clean up the coroutine such that it will no longer be in the table.
            cleanup(taskId, luaState);
            throw th;
        }

    }

    private void cleanup(final TaskId taskId, final LuaState luaState) {
        luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
        luaState.pushNil();
        luaState.setField(-2, taskId.toString());
        luaState.pop(1);
    }

    private void processYieldInstruction(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {

        final YieldInstruction instruction;
        instruction = luaState.getTop() > 1 ? luaState.checkEnum(0, YieldInstruction.values()) : IMMEDIATE;

        switch (instruction) {
            case FOR:
                scheduleFor(taskId, luaState, logAssist);
                break;
            case UNTIL_TIME:
                scheduleUntil(taskId, luaState, logAssist);
                break;
            case IMMEDIATE:
                scheduleImmediate(taskId, logAssist);
                break;
            case UNTIL_NEXT:
                scheduleUntilNextCron(taskId, luaState, logAssist);
                break;
            default:
                throw new InternalException("unknown enum value " + instruction);
        }

    }

    private void scheduleImmediate(final TaskId taskId, final LogAssist logAssist) {
        getScheduler().resumeTask(getLuaResource().getId(), taskId);
    }

    private void scheduleUntil(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final long delay = delayUntilMilliseconds(luaState);
        getScheduler().resumeTaskAfterDelay(getLuaResource().getId(), delay, MILLISECONDS, taskId);
    }

    private long delayUntilMilliseconds(final LuaState luaState) {
        final long now = currentTimeMillis();
        final long value = timeValueInMilliseconds(luaState);
        return max(0, value - now);
    }

    private void scheduleFor(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
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
        return luaState.getTop() > 2 ? luaState.checkEnum(2, TimeUnit.values()) : TimeUnit.SECONDS;
    }

    private void scheduleUntilNextCron(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
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
        return luaState -> {

            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.info("Loading module {} - {}", name, module.getChunkName());

            // This sets up the table which tracks and manages active tasks.

            luaState.newTable();
            luaState.setField(REGISTRYINDEX, COROUTINES_TABLE);

            // The actual function table
            luaState.newTable();

            luaState.pushJavaFunction(start);
            luaState.setField(-2, START);

            luaState.pushJavaFunction(resume);
            luaState.setField(-2, RESUME);

            return 1;

        };
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public LuaResource getLuaResource() {
        return luaResource;
    }

}
