package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.*;
import com.namazustudios.socialengine.rt.lua.builtin.BuiltinManager;
import com.namazustudios.socialengine.rt.lua.builtin.IndexDetailBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.JavaObjectBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.ResourceDetailBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.CoroutineBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.ResumeReasonBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.YieldInstructionBuiltin;
import com.namazustudios.socialengine.rt.lua.persist.Persistence;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.jnlua.LuaState.*;
import static com.namazustudios.socialengine.jnlua.LuaState.YIELD;
import static com.namazustudios.socialengine.rt.Path.fromPathString;
import static com.namazustudios.socialengine.rt.lua.Constants.*;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.ResumeReason.*;

/**
 * The abstract {@link Resource} type backed by a Lua script.  This uses the JNLua implentation
 * to drive the script.
 *
 * Note that this eschews the traditional static {@link Logger} instance, and creates an individual
 * instance named for the script itself.
 *
 * Created by patricktwohig on 8/25/15.
 */
public class LuaResource implements Resource {

    public static final String MODULE = "namazu.module";

    public static final String RESOURCE_BUILTIN = "namazu.resource.this";

    private static final Logger logger = LoggerFactory.getLogger(LuaResource.class);

    private final Map<TaskId, PendingTask> taskIdPendingTaskMap = new HashMap<>();

    private ResourceId resourceId = new ResourceId();

    private Attributes attributes = Attributes.emptyAttributes();

    private final LuaState luaState;

    private final LogAssist logAssist;

    private final Persistence persistence;

    private final BuiltinManager builtinManager;

    private final ResourceAcquisition resourceAcquisition;

    private Logger scriptLog = logger;

    /**
     * Redirects the assert function to one that plays nicely with the logger
     */
    private final JavaFunction scriptAssert = new ScriptAssert(s -> getScriptLog().error("{}", s));

    /**
     * Redirects the print function to the logger returned by {@link #getScriptLog()}.
     */
    private final JavaFunction printToScriptLog = new ScriptLogger(s -> getScriptLog().info("{}", s));

    /**
     * Creates an instance of {@link LuaResource} with the given {@link LuaState}
     * type.j
     *
     * If instantiation fails, it is the responsiblity of the caller to deallocate the {@link LuaState}
     * object.  If the constructor completes without error, then the caller does not need to close
     * the state as it will be handled by this object's {@link #close()} method.
     *
     * @param luaState the luaState
     */
    @Inject
    public LuaResource(final LuaState luaState, final Context context, final ResourceAcquisition resourceAcquisition) {
        try {

            this.luaState = luaState;
            this.logAssist = new LogAssist(this::getScriptLog, this::getLuaState);
            this.persistence = new Persistence(this, this::getScriptLog);
            this.builtinManager = new BuiltinManager(this::getLuaState, this::getScriptLog, persistence);
            this.resourceAcquisition = resourceAcquisition;

            openLibs();
            setupFunctionOverrides();
            getBuiltinManager().installBuiltin(new JavaObjectBuiltin<>(RESOURCE_BUILTIN, this));
            getBuiltinManager().installBuiltin(new CoroutineBuiltin(this, context.getSchedulerContext()));
            getBuiltinManager().installBuiltin(new ResourceDetailBuiltin(this, context));
            getBuiltinManager().installBuiltin(new IndexDetailBuiltin(this, context));
            getBuiltinManager().installBuiltin(new YieldInstructionBuiltin());
            getBuiltinManager().installBuiltin(new ResumeReasonBuiltin());

        } catch (Throwable th) {
            luaState.close();
            throw th;
        }
    }

    private void openLibs() {

        luaState.openLibs();
        luaState.rawGet(REGISTRYINDEX, RIDX_GLOBALS);

        luaState.pushNil();
        while (luaState.next(-2)) {

            final String name;

            if (!luaState.rawEqual(-1, -3)) {

                // Copies the key name on the stack and then extracts it.
                luaState.pushValue(-2);
                name = luaState.toString(-1);
                luaState.pop(1);

                // Adds it as a permanent object and then pops it off the stack.
                persistence.addPermanentObject(-1, LuaResource.class, name);

            }

            luaState.pop(1);

        }

        luaState.pop(1);

    }

    private void setupFunctionOverrides() {

        // We hijack the standard lua functions to better log output.  We also have to make them persistence aware so
        // they are properly serialized and deserialized.

        luaState.pushJavaFunction(scriptAssert);
        luaState.setGlobal(ASSERT_FUNCTION);

        luaState.pushJavaFunction(printToScriptLog);
        luaState.setGlobal(PRINT_FUNCTION);

        persistence.addPermanentJavaObject(scriptAssert, LuaResource.class, ASSERT_FUNCTION);
        persistence.addPermanentJavaObject(printToScriptLog, LuaResource.class, PRINT_FUNCTION);

    }

    @Override
    public ResourceId getId() {
        return resourceId;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     * Loads the main module of the script.  This should correspond to a module name as by the parameters of the
     * {@link ResourceLoader#load(String, Object...)}.  The resource internally uses a table in the registry to
     * store the module execution as not to pollute any of the global state.
     *
     * @param moduleName the name of the module
     * @param params the parameters to pass to the underlying {@link Resource}
     *
     * @throws IOException if the loading fails
     */
    public void loadModule(final AssetLoader assetLoader, final String moduleName, final Object ... params) {

        final LuaState luaState = getLuaState();
        final Path modulePath = fromPathString(moduleName, ".").appendExtension(Constants.LUA_FILE_EXT);

        try (final InputStream inputStream = assetLoader.open(modulePath)) {

            // We substitute the logger for the name of the file we actually are trying to open.  This way the
            // actual logger reads the name of the source file.
            scriptLog = LoggerFactory.getLogger(modulePath.toNormalizedPathString());
            luaState.load(inputStream, moduleName, "bt");
            scriptLog.debug("Loaded script {}", moduleName);

            for (final Object object : params) {
                luaState.pushJavaObject(object);
            }

            luaState.call(params.length, 1);

            if (luaState.isNil(-1)) {
                throw new ModuleNotFoundException("got nil module for " + moduleName);
            }

            luaState.setField(REGISTRYINDEX, MODULE);

        } catch (IOException ex) {
            logAssist.error("Failed to load script.", ex);
            throw new InternalException(ex);
        } catch (AssetNotFoundException ex) {
            logAssist.error("Module not found: " + moduleName, ex);
            throw new ModuleNotFoundException(ex);
        } finally {
            if (luaState.isOpen()) {
                luaState.setTop(0);
            }
        }

    }

    @Override
    public void setVerbose(boolean verbose) {
        luaState.pushBoolean(verbose);
        luaState.setPersistenceSetting("path", -1);
    }

    @Override
    public boolean isVerbose() {
        try {
            luaState.getPersistenceSetting("debug");
            return luaState.toBoolean(-1);
        } finally {
            luaState.pop(1);
        }
    }

    @Override
    public void serialize(OutputStream os) throws IOException {
        getPersistence().serialize(os);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        getPersistence().deserialize(is, sh -> {
            resourceId = sh.getResourceId();
            attributes = sh.getAttributes();
        });
    }

    /**
     * Invokes {@link LuaState#close()} and removes any resources from memory.  After this is called, this
     * {@link LuaResource} may not be reused.
     *
     * @see {@link Resource#close()}
     *
     */
    @Override
    public void close() {

        taskIdPendingTaskMap.values().forEach(pendingTask -> {
            final ResourceDestroyedException resourceDestroyedException = new ResourceDestroyedException(getId());
            pendingTask.fail(resourceDestroyedException);
        });

        getLuaState().close();

    }

    @Override
    public MethodDispatcher getMethodDispatcher(final String name) {
        return params -> (consumer, throwableConsumer) -> {

            final LuaState luaState = getLuaState();
            FinallyAction finalOperation = () -> luaState.setTop(0);

            try {

                luaState.getGlobal(REQUIRE);
                luaState.pushString(CoroutineBuiltin.MODULE_NAME);
                luaState.call(1, 1);
                luaState.getField(-1, CoroutineBuiltin.START);
                luaState.remove(-2);

                luaState.getField(REGISTRYINDEX, MODULE);
                luaState.getField(-1, name);
                luaState.remove(-2);

                if (!luaState.isFunction(-1)){
                    getScriptLog().error("No such method {}", name);
                    throw new MethodNotFoundException("No such method: " + name);
                }

                luaState.newThread();
                for (Object param : params) luaState.pushJavaObject(param);

                luaState.call(params.length + 1, 3);

                final TaskId taskId = new TaskId(luaState.checkString(1));            // task id
                final int status = luaState.checkInteger(2);                          // thread status
                final Object result = luaState.checkJavaObject(3, Object.class);      // result
                final PendingTask pendingTask = new PendingTask(taskId, consumer, throwableConsumer);

                if (status == YIELD) {
                    resourceAcquisition.acquire(getId());
                    taskIdPendingTaskMap.put(taskId, pendingTask);
                } else {
                    pendingTask.finish(result);
                }

                return taskId;

            } catch (Exception ex) {
                logAssist.error("Error dispatching method: " + name, ex);
                throw ex;
            } finally {
                finalOperation.perform();
            }

        };
    }

    @Override
    public void resumeFromNetwork(final TaskId taskId, final Object networkResult) {

        final PendingTask pendingTask = taskIdPendingTaskMap.getOrDefault(taskId, new PendingTask(taskId,
            o -> scriptLog.debug("Discarding {} for task {}", o, taskId),
            e -> scriptLog.debug("Discarding exception for task {}", taskId, e)));

        final LuaState luaState = getLuaState();
        FinallyAction finalOperation = () -> luaState.setTop(0);

        try {

            luaState.getGlobal(REQUIRE);
            luaState.pushString(CoroutineBuiltin.MODULE_NAME);
            luaState.call(1, 1);
            luaState.getField(-1, CoroutineBuiltin.RESUME);
            luaState.remove(1);

            luaState.pushString(taskId.asString());
            luaState.pushString(NETWORK.toString());
            luaState.pushJavaObject(networkResult);
            luaState.call(3, 3);

            if (luaState.isNil(1)) {
                throw new NoSuchTaskException(taskId);
            }

            final String taskIdString = luaState.checkString(1);                        // task id
            final int status = luaState.checkInteger(2);                                // thread status
            luaState.checkJavaObject(3, Object.class);                                  // the return value

            if (!taskId.asString().equals(taskIdString)) {
                getScriptLog().error("Mismatched task id {} != {}", taskId, taskIdString);
                throw new IllegalStateException("task ID mismatch");
            } else if (status == YIELD) {
                getScriptLog().debug("Resuming task {} from network yielded.  Resuming later.", taskId);
            } else {
                resourceAcquisition.release(getId());
            }

        } catch (NoSuchTaskException ex) {
            throw ex;
        } catch (Exception ex) {
            getScriptLog().error("Caught exception resuming task {}.", taskId, ex);
            pendingTask.fail(ex);
            throw ex;
        } finally {
            finalOperation.perform();
        }

    }

    @Override
    public void resumeWithError(TaskId taskId, Throwable throwable) {

        final PendingTask pendingTask = taskIdPendingTaskMap.getOrDefault(taskId, new PendingTask(taskId,
            o -> scriptLog.debug("Discarding {} for task {}", o, taskId),
            e -> scriptLog.debug("Discarding exception for task {}", taskId, e)));

        final LuaState luaState = getLuaState();
        FinallyAction finalOperation = () -> luaState.setTop(0);

        final ResponseCode responseCode = throwable instanceof BaseException ?
            ((BaseException)throwable).getResponseCode() :
            ResponseCode.INTERNAL_ERROR_FATAL;

        try {

            luaState.getGlobal(REQUIRE);
            luaState.pushString(CoroutineBuiltin.MODULE_NAME);
            luaState.call(1, 1);
            luaState.getField(-1, CoroutineBuiltin.RESUME);
            luaState.remove(1);

            luaState.pushString(taskId.asString());
            luaState.pushString(ERROR.toString());
            luaState.pushInteger(responseCode.getCode());
            luaState.call(3, 3);

            if (luaState.isNil(1)) {
                throw new NoSuchTaskException(taskId);
            }

            final String taskIdString = luaState.checkString(1);                        // task id
            final int status = luaState.checkInteger(2);                                // thread status

            if (!taskId.asString().equals(taskIdString)) {
                getScriptLog().error("Mismatched task id {} != {}", taskId, taskIdString);
                throw new IllegalStateException("task ID mismatch");
            } else if (status == YIELD) {
                getScriptLog().debug("Resuming task {} with error yielded.  Resuming later.", taskId);
            } else {
                resourceAcquisition.release(getId());
            }

        } catch (NoSuchTaskException ex) {
            throw ex;
        } catch (Exception ex) {
            getScriptLog().error("Caught exception resuming task {}.", taskId, ex);
            pendingTask.fail(ex);
        } finally {
            finalOperation.perform();
        }

    }

    @Override
    public void resumeFromScheduler(final TaskId taskId, final double elapsedTime) {

        final PendingTask pendingTask = taskIdPendingTaskMap.getOrDefault(taskId, new PendingTask(taskId,
            o -> scriptLog.info("Discarding {} for task {}", o, taskId),
            e -> scriptLog.info("Discarding exception for task {}", taskId, e)));

        final LuaState luaState = getLuaState();
        FinallyAction finalOperation = () -> luaState.setTop(0);

        try {

            luaState.getGlobal(REQUIRE);
            luaState.pushString(CoroutineBuiltin.MODULE_NAME);
            luaState.call(1, 1);
            luaState.getField(-1, CoroutineBuiltin.RESUME);
            luaState.remove(1);

            luaState.pushString(taskId.asString());
            luaState.pushString(SCHEDULER.toString());
            luaState.pushNumber(elapsedTime);
            luaState.pushString(TimeUnit.SECONDS.toString());
            luaState.call(4, 3);

            if (luaState.isNil(1)) {
                throw new NoSuchTaskException(taskId);
            }

            final String taskIdString = luaState.checkString(1);                        // task id
            final int status = luaState.checkInteger(2);                                // thread status

            if (!taskId.asString().equals(taskIdString)) {
                getScriptLog().error("Mismatched task id {} != {}", taskId, taskIdString);
                throw new IllegalStateException("task ID mismatch");
            } else if (status == YIELD) {
                getScriptLog().debug("Scheduler resumed task {} yielded.  Resuming later.", taskId);
            } else {
                resourceAcquisition.release(getId());
            }

        } catch (NoSuchTaskException ex) {
            throw ex;
        } catch (Exception ex) {
            getScriptLog().error("Caught exception resuming task {}.", taskId, ex);
            pendingTask.throwableConsumer.accept(ex);
        } finally {
            finalOperation.perform();
        }

    }

    /**
     * Finishes a pending task by driving the associated {@link Consumer<Object>} used to receive the successful
     * result.
     *
     * @param taskId the {@link TaskId} of the task to fail
     * @param result the result {@link Object}
     */
    public void finishPendingTask(final TaskId taskId, final Object result) {

        final PendingTask pendingTask = taskIdPendingTaskMap.remove(taskId);

        if (pendingTask != null) {
            pendingTask.finish(result);
        }

    }

    /**
     * Fails a {@link PendingTask} with the supplied {@link Throwable}, handing it to the associated
     * {@Link Consumer<Throwable>}.
     *
     * @param taskId the {@link TaskId} of the task to fail
     * @param error the {@link Throwable} which caused the failure
     */
    public void failPendingTask(final TaskId taskId, final Throwable error) {

        final PendingTask pendingTask = taskIdPendingTaskMap.remove(taskId);

        if (pendingTask != null) {
            pendingTask.fail(error);
        }

    }

    /**
     * Gets the {@link BuiltinManager} used by this {@link LuaResource}.
     *
     * @return the {@link BuiltinManager}
     */
    public BuiltinManager getBuiltinManager() {
        return builtinManager;
    }

    /**
     * Gets the {@link LuaState} backing this {@link LuaResource}.
     *
     * @return the {@link LuaState} instance
     */
    public LuaState getLuaState() {
        return luaState;
    }

    /**
     * Gets the {@link Persistence} instance used by this {@link LuaResource}.
     *
     * @return the {@link Persistence} instance
     */
    public Persistence getPersistence() {
        return persistence;
    }

    /**
     * Gets a special instance of {@link Logger} which the script can use for script logging.
     *
     * @return the {@link Logger} instance
     */
    public Logger getScriptLog() {
        return scriptLog;
    }

    private static class PendingTask {

        private final TaskId taskId;

        private final Consumer<Object> resultConsumer;

        private final Consumer<Throwable> throwableConsumer;

        public PendingTask(
                final TaskId taskId,
                final Consumer<Object> resultConsumer,
                final Consumer<Throwable> throwableConsumer) {
            this.taskId = taskId;
            this.resultConsumer = resultConsumer;
            this.throwableConsumer = throwableConsumer;
        }

        public void finish(final Object result) {
            try {
                resultConsumer.accept(result);
            } catch (Exception ex) {
                fail(ex);
            }
        }

        public void fail(final Throwable th) {
            try {
                logger.error("Task failed with exception.", taskId, th);
                throwableConsumer.accept(th);
            } catch (Exception ex) {
                logger.error("Caught exception failing task {} ", taskId, ex);
            }
        }

    }

}
