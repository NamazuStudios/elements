package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.jnlua.LuaType;
import com.namazustudios.socialengine.rt.SchedulerContext;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import com.namazustudios.socialengine.rt.lua.persist.Persistence;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.namazustudios.socialengine.jnlua.LuaState.REGISTRYINDEX;
import static com.namazustudios.socialengine.jnlua.LuaState.YIELD;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.YieldInstruction.IMMEDIATE;
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

    public static final String CURRENT_TASK_ID = "current_task_id";

    private final LuaResource luaResource;

    private final SchedulerContext schedulerContext;

    private TaskId runningTaskId;

    public CoroutineBuiltin(final LuaResource luaResource, final SchedulerContext schedulerContext) {
        this.luaResource = luaResource;
        this.schedulerContext = schedulerContext;
    }

    /**
     * Accepts no arguments and simply returns a string indicating the currently running task.  This can be used to
     * within the body of a coroutine to get the currently-running
     */
    private final JavaFunction currentTaskId = luaState -> {

        final LogAssist logAssist = new LogAssist(getLuaResource()::getScriptLog, () -> luaState);

        try {

            if (runningTaskId == null) {
                logger.error("No running task.  Can only be called within the context of a managed coroutine.");
            }

            luaState.setTop(0);
            luaState.pushString(runningTaskId.asString());

            return 1;

        } catch (Throwable th) {
            logAssist.error("Could not start coroutine.", th);
            throw th;
        }

    };

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

            final TaskId taskId = new TaskId(getLuaResource().getId());

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
            luaState.getField(-1, taskId.asString());

            if (!luaState.isThread(-1)) {
                logger.debug("no such task " + taskId + " instead got " + luaState.typeName(-1));
                return 0;
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

            final int returned;
            final TaskId existingRunningTaskId = this.runningTaskId;

            try {
                runningTaskId = taskId;
                returned = luaState.resume(1, luaState.getTop() - 1);
            } finally {
                runningTaskId = existingRunningTaskId;
            }

            // Check the status of the coroutine.  If it is a yield, then we process the yield instructions which will
            // reschedule the task if necessary.  If there's a successful completion, then we collect the results
            // of the method and push them on the stack.

            final int status = luaState.status(1);

            if (status == YIELD) {

                // If we yielded, then we start looking for the yield instructions and process them.  If the yield
                // instructions weren't passed properly, an exception will result.

                luaState.remove(1);
                processYieldInstruction(taskId, luaState, logAssist);

                // Now that all instructions are processed, we return the status and the task id.

                luaState.setTop(0);
                luaState.pushString(taskId.asString());
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

                luaState.pushString(taskId.asString());
                luaState.insert(1);

                final Object result = luaState.getTop() == 2 ? null : luaState.checkJavaObject(3, Object.class);
                getLuaResource().finishPendingTask(taskId, result);

                return returned + 2;

            }

        } catch (Throwable th) {
            // All exceptions will clean up the coroutine such that it will no longer be in the table.
            cleanup(taskId, luaState);
            getLuaResource().failPendingTask(taskId, th);
            throw th;
        }

    }

    private void cleanup(final TaskId taskId, final LuaState luaState) {
        luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
        luaState.pushNil();
        luaState.setField(-2, taskId.asString());
        luaState.pop(1);
    }

    private void processYieldInstruction(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {

        final YieldInstruction instruction;
        instruction = luaState.getTop() > 0 ? luaState.checkEnum(1, YieldInstruction.values()) : IMMEDIATE;

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
            case INDEFINITELY:
                break;
            default:
                throw new InternalException("unknown enum value " + instruction);
        }

    }

    private void scheduleImmediate(final TaskId taskId, final LogAssist logAssist) {
        getSchedulerContext().resumeTaskAfterDelay(0, MILLISECONDS, taskId);
    }

    private void scheduleUntil(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final long delay = delayUntilMilliseconds(luaState);
        getSchedulerContext().resumeTaskAfterDelay(delay, MILLISECONDS, taskId);
    }

    private long delayUntilMilliseconds(final LuaState luaState) {
        final long now = currentTimeMillis();
        final long value = timeValueInMilliseconds(luaState);
        return max(0, value - now);
    }

    private void scheduleFor(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final long delay = timeValueInMilliseconds(luaState);
        getSchedulerContext().resumeTaskAfterDelay(delay, MILLISECONDS, taskId);
    }

    private long timeValueInMilliseconds(final LuaState luaState) {

         if (luaState.getTop() < 2) {
            throw new IllegalArgumentException("time value must be specified");
        }

        final TimeUnit timeUnit = timeUnit(luaState);
        final double value = luaState.checkNumber(2) * TIME_UNIT_CORRECTION_FACTOR_D;

        return MILLISECONDS.convert(round(value), timeUnit) / TIME_UNIT_CORRECTION_FACTOR_L;

    }

    private TimeUnit timeUnit(final LuaState luaState) {
        return luaState.getTop() > 2 ? luaState.checkEnum(3, TimeUnit.values()) : TimeUnit.SECONDS;
    }

    private void scheduleUntilNextCron(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final long delay = delayUntilNextCronMilliseconds(luaState);
        getSchedulerContext().resumeTaskAfterDelay(delay, MILLISECONDS, taskId);
    }

    private long delayUntilNextCronMilliseconds(final LuaState luaState) {

        if (luaState.getTop() < 2) {
            throw new IllegalArgumentException("time value must be specified");
        }

        final String expression = luaState.checkString(2);
        final CronExpression cronExpression;

        try {
            cronExpression = new CronExpression(expression);
        } catch (ParseException ex) {
            throw new InternalException(ex);
        }

        final Date when = cronExpression.getNextValidTimeAfter(new Date());
        return max(0l, when.getTime() - currentTimeMillis());

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
            logger.debug("Loading module {} - {}", name, module.getChunkName());

            // This sets up the table which tracks and manages active tasks.

            luaState.newTable();
            luaState.setField(REGISTRYINDEX, COROUTINES_TABLE);

            // The actual function table
            luaState.newTable();

            luaState.pushJavaFunction(start);
            luaState.setField(-2, START);

            luaState.pushJavaFunction(resume);
            luaState.setField(-2, RESUME);

            luaState.pushJavaFunction(currentTaskId);
            luaState.setField(-2, CURRENT_TASK_ID);

            return 1;

        };
    }

    @Override
    public void makePersistenceAware(final Persistence persistence) {
        persistence.addPermanentJavaObject(start, CoroutineBuiltin.class, START);
        persistence.addPermanentJavaObject(resume, CoroutineBuiltin.class, RESUME);
        persistence.addPermanentJavaObject(currentTaskId, CoroutineBuiltin.class, CURRENT_TASK_ID);
    }

    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    public LuaResource getLuaResource() {
        return luaResource;
    }

}
