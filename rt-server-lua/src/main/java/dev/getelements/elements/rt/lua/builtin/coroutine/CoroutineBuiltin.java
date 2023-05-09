package dev.getelements.elements.rt.lua.builtin.coroutine;

import dev.getelements.elements.jnlua.JavaFunction;
import dev.getelements.elements.jnlua.LuaState;
import dev.getelements.elements.jnlua.LuaType;
import dev.getelements.elements.rt.CurrentResource;
import dev.getelements.elements.rt.PersistenceStrategy;
import dev.getelements.elements.rt.annotation.*;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.id.TaskId;
import dev.getelements.elements.rt.lua.LogAssist;
import dev.getelements.elements.rt.lua.LuaResource;
import dev.getelements.elements.rt.lua.builtin.Builtin;
import dev.getelements.elements.rt.lua.persist.ErisPersistence;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.jnlua.LuaState.REGISTRYINDEX;
import static dev.getelements.elements.jnlua.LuaState.YIELD;
import static dev.getelements.elements.rt.lua.builtin.coroutine.YieldInstruction.IMMEDIATE;
import static java.lang.Math.max;
import static java.lang.StrictMath.round;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Intrinsic(
    value = @ModuleDefinition("namazu.coroutine"),
    authors = "ptwohig",
    summary = "System-managed Coroutines.",
    description = "This API controls the managed coroutines used by the system allowing hte caller to create, " +
                  "manage, and schedule tasks modeled as coroutines. Coroutines in this module have special yield " +
                  "semantics enabling the system to manage the lifecycle of the VMs contained in the cluster.",
    methods = {
        @MethodDefinition(
            value = "start",
            summary = "Starts a system-managed coroutine.",
            description =
                "Starts the coroutine and assigns it a task id. The system will manage this coroutine until " +
                "it fails with an error, exits, is explicitly killed, or its associated resource is " +
                "destroyed.",
            parameters = {
                @ParameterDefinition(value="coroutine", type = "thread", comment = "The coroutine to start.")
            },
            returns = {
                @ReturnDefinition(comment = "the status (yield or exit).", type = "string"),
                @ReturnDefinition(comment = "the system-assigned task ID.", type = "string"),
                @ReturnDefinition(comment = "if the coroutine finished, all remaining execution results.", type = "..."),
            }
        ),
        @MethodDefinition(
            value = "resume",
            summary = "Resumes a system-managed coroutine.",
            description =
                "Starts the coroutine and assigns it a task id. The system will manage this coroutine until " +
                "it fails with an error, exits, is explicitly killed, or its associated resource is " +
                "destroyed.",
            parameters = {
                @ParameterDefinition(value="task_id", type = "string", comment = "The system-managed task ID to resume.")
            },
            returns = {
                @ReturnDefinition(comment = "the status (yield or exit).", type = "string"),
                @ReturnDefinition(comment = "the system-assigned task ID.", type = "string"),
                @ReturnDefinition(comment = "if the coroutine finished, all remaining execution results.", type = "..."),
            }
        ),
        @MethodDefinition(
            value = "current_task_id",
            summary = "Returns the current Task ID.",
            description =
                "The currently executing Task ID. This is always set by the system. All executions must be happening" +
                "within the scope of a Task ID, except for the initial startup and loading of the script.",
            parameters = {
                @ParameterDefinition(value="task_id", type = "string", comment = "The system-managed task ID to resume.")
            }
        )
    }
)
public class CoroutineBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(CoroutineBuiltin.class);

    private static final long TIME_UNIT_CORRECTION_FACTOR_L = 10000;

    private static final double TIME_UNIT_CORRECTION_FACTOR_D = TIME_UNIT_CORRECTION_FACTOR_L;

    public static final String MODULE_NAME = "namazu.coroutine";

    public static final String COROUTINES_TABLE = "dev.getelements.elements.rt.lua.builtin.coroutine.CoroutineBuiltin.coroutines";

    public static final String START = "start";

    public static final String RESUME = "resume";

    public static final String CURRENT_TASK_ID = "current_task_id";

    private final LuaResource luaResource;

    private final PersistenceStrategy persistenceStrategy;

    private TaskId runningTaskId;

    public CoroutineBuiltin(final LuaResource luaResource,
                            final PersistenceStrategy persistenceStrategy) {
        this.luaResource = luaResource;
        this.persistenceStrategy = persistenceStrategy;
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

            luaState.replace(1);  // Places the thread at the first index in the stack, replacing the TaskId
            luaState.pop(1);      // Pops the coroutine registry table as it is no longer needed

            return resume(taskId, luaState, logAssist);

        } catch (Throwable th) {
            logAssist.error("Could not start coroutine.", th);
            throw th;
        }

    };

    private int resume(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        try {
            do {

                // Execute the coroutine/thread.  Remember, if the thread is not in the right state, this may cause the
                // thread to fail.  This shouldn't happen because it should be deregistered.

                final int returned;
                final TaskId existingRunningTaskId = this.runningTaskId;

                try (var c = CurrentResource.getInstance().enter(getLuaResource())) {
                    runningTaskId = taskId;
                    returned = luaState.resume(1, luaState.getTop() - 1);
                } finally {
                    runningTaskId = existingRunningTaskId;
                }

                // Check the status of the coroutine.  If it is a yield, then we process the yield instructions which
                // will reschedule the task if necessary.  If there's a successful completion, then we collect the
                // results of the method and push them on the stack.

                final int status = luaState.status(1);

                if (status == YIELD) {

                    // If we yielded, then we start looking for the yield instructions and process them.  If the yield
                    // instructions weren't passed properly, an exception will result.

                    if (processYieldInstruction(taskId, luaState, logAssist)) {
                        // If the yield instruction indicates that we need to continue execution after the yield, we
                        // will do exactly that.  All arguments returned from the yield will be simply passed back
                        // through the yield if the situation warrants it.  The next iteration will simply process
                        // the instructions as they were.
                        continue;
                    }

                    // Now that all instructions are processed, we return the status and the task id.

                    luaState.setTop(0);
                    luaState.pushString(taskId.asString());
                    luaState.pushInteger(status);

                    return 2;

                } else {

                    // If the coroutine wasn't yielded because it finished normally, then we simply take what's on the
                    // stack and return it to the caller.  The caller then will collect the values that are returned.
                    // However, we do prepend the task ID and status so the caller can make sense of the execution
                    // result.

                    luaState.remove(1);
                    cleanup(taskId, luaState);

                    luaState.pushInteger(status);
                    luaState.insert(1);

                    luaState.pushString(taskId.asString());
                    luaState.insert(1);

                    final Object result = luaState.getTop() == 2 ? null : luaState.checkJavaObject(3, Object.class);
                    getLuaResource().getLocalContextOrContextFor(taskId).getTaskContext().finishWithResult(taskId, result);

                    return returned + 2;

                }

            } while (true);
        } catch (Throwable th) {
            // All exceptions will clean up the coroutine such that it will no longer be in the table.
            cleanup(taskId, luaState);
            getLuaResource().getLocalContextOrContextFor(taskId).getTaskContext().finishWithError(taskId, th);
            throw th;
        }
    }

    private void cleanup(final TaskId taskId, final LuaState luaState) {
        luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);
        luaState.pushNil();
        luaState.setField(-2, taskId.asString());
        luaState.pop(1);
    }

    private boolean processYieldInstruction(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {

        final YieldInstruction instruction;
        instruction = luaState.getTop() > 0 ? luaState.checkEnum(2, YieldInstruction.values()) : IMMEDIATE;

        switch (instruction) {
            case FOR:
                scheduleFor(taskId, luaState, logAssist);
                return false;
            case UNTIL_TIME:
                scheduleUntil(taskId, luaState, logAssist);
                return false;
            case IMMEDIATE:
                scheduleImmediate(taskId, logAssist);
                return false;
            case UNTIL_NEXT:
                scheduleUntilNextCron(taskId, luaState, logAssist);
                return false;
            case INDEFINITELY:
                return false;
            case COMMIT:
                persist(taskId, luaState, logAssist);
                return true;
            default:
                throw new InternalException("unknown enum value " + instruction);
        }

    }

    private void scheduleImmediate(final TaskId taskId, final LogAssist logAssist) {
        getLuaResource().getLocalContextOrContextFor(taskId).getSchedulerContext().resumeTaskAfterDelay(taskId, 0, MILLISECONDS);
    }

    private void scheduleUntil(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final long delay = delayUntilMilliseconds(luaState);
        getLuaResource().getLocalContextOrContextFor(taskId).getSchedulerContext().resumeTaskAfterDelay(taskId, delay, MILLISECONDS);
    }

    private long delayUntilMilliseconds(final LuaState luaState) {
        final long now = currentTimeMillis();
        final long value = timeValueInMilliseconds(luaState);
        return max(0, value - now);
    }

    private void scheduleFor(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final long delay = timeValueInMilliseconds(luaState);
        getLuaResource().getLocalContextOrContextFor(taskId).getSchedulerContext().resumeTaskAfterDelay(taskId, delay, MILLISECONDS);
    }

    private long timeValueInMilliseconds(final LuaState luaState) {

         if (luaState.getTop() < 3) {
            throw new IllegalArgumentException("time value must be specified");
        }

        final TimeUnit timeUnit = timeUnit(luaState);
        final double value = luaState.checkNumber(3) * TIME_UNIT_CORRECTION_FACTOR_D;

        return MILLISECONDS.convert(round(value), timeUnit) / TIME_UNIT_CORRECTION_FACTOR_L;

    }

    private TimeUnit timeUnit(final LuaState luaState) {
        return luaState.getTop() > 2 ? luaState.checkEnum(4, TimeUnit.values()) : TimeUnit.SECONDS;
    }

    private void scheduleUntilNextCron(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final long delay = delayUntilNextCronMilliseconds(luaState);
        getLuaResource().getLocalContextOrContextFor(taskId).getSchedulerContext().resumeTaskAfterDelay(taskId, delay, MILLISECONDS);
    }

    private long delayUntilNextCronMilliseconds(final LuaState luaState) {

        if (luaState.getTop() < 2) {
            throw new IllegalArgumentException("time value must be specified");
        }

        final String expression = luaState.checkString(3);
        final CronExpression cronExpression;

        try {
            cronExpression = new CronExpression(expression);
        } catch (ParseException ex) {
            throw new InternalException(ex);
        }

        final Date when = cronExpression.getNextValidTimeAfter(new Date());
        return max(0l, when.getTime() - currentTimeMillis());

    }

    private void persist(final TaskId taskId, final LuaState luaState, final LogAssist logAssist) {
        final ResourceId resourceId = getLuaResource().getId();
        getPersistenceStrategy().persist(resourceId);
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

            luaState.getField(REGISTRYINDEX, COROUTINES_TABLE);

            if (!luaState.isTable(-1)) {
                // We make the table if no such table exists in the lua resource already.  This ensures that the table
                // has been created if it does not exist already.
                luaState.newTable();
                luaState.setField(REGISTRYINDEX, COROUTINES_TABLE);
            } else {
                // Persitence may have already set this table up so we want to make sure that the persistence table is
                // only created if it has not been already made.  There's probably a better way to handle this but for
                // now we just doa  quick check to make sure that it's not been made yet.
                luaState.pop(1);
            }

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
    public void makePersistenceAware(final ErisPersistence erisPersistence) {
        erisPersistence.addPermanentJavaObject(start, CoroutineBuiltin.class, START);
        erisPersistence.addPermanentJavaObject(resume, CoroutineBuiltin.class, RESUME);
        erisPersistence.addPermanentJavaObject(currentTaskId, CoroutineBuiltin.class, CURRENT_TASK_ID);
    }

    public LuaResource getLuaResource() {
        return luaResource;
    }

    public PersistenceStrategy getPersistenceStrategy() {
        return persistenceStrategy;
    }

}
