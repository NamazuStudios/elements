package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.*;
import com.namazustudios.socialengine.rt.lua.builtin.BuiltinManager;
import com.namazustudios.socialengine.rt.lua.builtin.JavaObjectBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.ResourceDetailBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.CoroutineBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.ResumeReasonBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.YieldInstructionBuiltin;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.naef.jnlua.LuaState.REGISTRYINDEX;
import static com.naef.jnlua.LuaState.YIELD;
import static com.namazustudios.socialengine.rt.Path.fromPathString;
import static com.namazustudios.socialengine.rt.lua.Constants.REQUIRE;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.ResumeReason.ERROR;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.ResumeReason.NETWORK;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.ResumeReason.SCHEDULER;

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

    private final ResourceId resourceId = new ResourceId();

    private final LuaState luaState;

    private final LogAssist logAssist;

    private final BuiltinManager builtinManager;

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
    public LuaResource(final LuaState luaState, final Context context, final Scheduler scheduler) {
        try {

            this.luaState = luaState;
            this.logAssist = new LogAssist(this::getScriptLog, this::getLuaState);
            this.builtinManager = new BuiltinManager(this::getLuaState, this::getScriptLog);

            luaState.openLibs();
            setupFunctionOverrides();
            getBuiltinManager().installBuiltin(new JavaObjectBuiltin<>(RESOURCE_BUILTIN, this));
            getBuiltinManager().installBuiltin(new CoroutineBuiltin(this, context.getSchedulerContext()));
            getBuiltinManager().installBuiltin(new ResourceDetailBuiltin(this, context));
            getBuiltinManager().installBuiltin(new YieldInstructionBuiltin());
            getBuiltinManager().installBuiltin(new ResumeReasonBuiltin());

        } catch (Throwable th) {
            luaState.close();
            throw th;
        }
    }

    @Override
    public ResourceId getId() {
        return resourceId;
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
            scriptLog.info("Loaded script {}", moduleName);

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

    private void setupFunctionOverrides() {

        // We hijack the standard lua functions to better log output

        luaState.pushJavaFunction(printToScriptLog);
        luaState.setGlobal(Constants.PRINT_FUNCTION);

        luaState.pushJavaFunction(scriptAssert);
        luaState.setGlobal(Constants.ASSERT_FUNCTION);

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

                    final String taskId = luaState.checkString(1);                        // task id
                    final int status = luaState.checkInteger(2);                          // thread status
                    final Object result = luaState.checkJavaObject(3, Object.class);      // the return value

                    if (status == YIELD) {
                        final PendingTask pendingTask = new PendingTask(consumer, throwableConsumer);
                        taskIdPendingTaskMap.put(new TaskId(taskId), pendingTask);
                    } else {
                        finalOperation = finalOperation.andThen(() -> consumer.accept(result));
                    }

                return new TaskId(taskId);

            } catch (Throwable th) {
                logAssist.error("Error dispatching method: " + name, th);
                throw th;
            } finally {
                finalOperation.perform();
            }

        };
    }

    @Override
    public void resumeFromNetwork(TaskId taskId, Object networkResult) {

        final PendingTask pendingTask = taskIdPendingTaskMap.get(taskId);

        if (pendingTask == null) {
            throw new InternalException("no pending task with id " + taskId);
        }

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

            final String taskIdString = luaState.checkString(1);                        // task id
            final int status = luaState.checkInteger(2);                                // thread status
            final Object result = luaState.checkJavaObject(3, Object.class);            // the return value

            if (!taskId.asString().equals(taskIdString)) {
                getScriptLog().error("Mismatched task id {} != {}", taskId, taskIdString);
                throw new IllegalStateException("task ID mismatch");
            } else if (status == YIELD) {
                getScriptLog().info("Task {} yielded.  Resuming later.", taskId);
            } else {
                finalOperation = finalOperation.andThen(() -> pendingTask.resultConsumer.accept(result));
            }

        } catch (Throwable th) {
            getScriptLog().error("Caught exception resuming task {}.", taskId, th);
            pendingTask.throwableConsumer.accept(th);
        } finally {
            finalOperation.perform();
        }

    }

    @Override
    public void resumeWithError(TaskId taskId, Throwable throwable) {

        final PendingTask pendingTask = taskIdPendingTaskMap.get(taskId);

        if (pendingTask == null) {
            throw new InternalException("no pending task with id " + taskId);
        }

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

            final String taskIdString = luaState.checkString(1);                        // task id
            final int status = luaState.checkInteger(2);                                // thread status
            final Object result = luaState.checkJavaObject(3, Object.class);            // the return value

            if (!taskId.asString().equals(taskIdString)) {
                getScriptLog().error("Mismatched task id {} != {}", taskId, taskIdString);
                throw new IllegalStateException("task ID mismatch");
            } else if (status == YIELD) {
                getScriptLog().info("Task {} yielded.  Resuming later.", taskId);
            } else {
                finalOperation = finalOperation.andThen(() -> pendingTask.resultConsumer.accept(result));
            }

        } catch (Throwable th) {
            getScriptLog().error("Caught exception resuming task {}.", taskId, th);
            pendingTask.throwableConsumer.accept(th);
        } finally {
            finalOperation.perform();
        }

    }

    @Override
    public void resumeFromScheduler(final TaskId taskId, final double elapsedTime) {

        final PendingTask pendingTask = taskIdPendingTaskMap.get(taskId);

        if (pendingTask == null) {
            throw new InternalException("no pending task with id " + taskId);
        }

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

            final String taskIdString = luaState.checkString(1);                        // task id
            final int status = luaState.checkInteger(2);                                // thread status
            final Object result = luaState.checkJavaObject(3, Object.class);            // the return value

            if (!taskId.asString().equals(taskIdString)) {
                getScriptLog().error("Mismatched task id {} != {}", taskId, taskIdString);
                throw new IllegalStateException("task ID mismatch");
            } else if (status == YIELD) {
                getScriptLog().info("Task {} yielded.  Resuming later.", taskId);
            } else {
                finalOperation = finalOperation.andThen(() -> pendingTask.resultConsumer.accept(result));
            }

        } catch (Throwable th) {
            getScriptLog().error("Caught exception resuming task {}.", taskId, th);
            pendingTask.throwableConsumer.accept(th);
        } finally {
            finalOperation.perform();
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
     * Gets a special instance of {@link Logger} which the script can use for script logging.
     *
     * @return the {@link Logger} instance
     */
    public Logger getScriptLog() {
        return scriptLog;
    }

    private static class PendingTask {

        private final Consumer<Object> resultConsumer;

        private final Consumer<Throwable> throwableConsumer;

        public PendingTask(Consumer<Object> resultConsumer, Consumer<Throwable> throwableConsumer) {
            this.resultConsumer = resultConsumer;
            this.throwableConsumer = throwableConsumer;
        }

    }

}
