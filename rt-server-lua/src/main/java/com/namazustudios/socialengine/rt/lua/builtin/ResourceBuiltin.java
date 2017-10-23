package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.CoroutineBuiltin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.namazustudios.socialengine.rt.lua.Constants.COROUTINE;
import static com.namazustudios.socialengine.rt.lua.Constants.REQUIRE;
import static com.namazustudios.socialengine.rt.lua.Constants.YIELD;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.YieldInstruction.*;

public class ResourceBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResourceBuiltin.class);

    public static final String MODULE_NAME = "namazu.resource";

    public static final String CREATE = "create";

    public static final String INVOKE = "invoke";

    public static final String INVOKE_PATH = "invoke_path";

    public static final String DESTROY = "destroy";

    private final LuaResource luaResource;

    private final Scheduler scheduler;

    private final ResourceContext resourceContext;

    public ResourceBuiltin(final LuaResource luaResource,
                           final Scheduler scheduler,
                           final ResourceContext resourceContext) {
        this.luaResource = luaResource;
        this.scheduler = scheduler;
        this.resourceContext = resourceContext;
    }

    private JavaFunction create = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final String module = luaState.checkString(1);
            final Path path = new Path(luaState.checkString(2));
            final Object[] params = luaState.checkJavaObject(3, Object[].class);

            final TaskId taskId = currentTaskId(luaState);
            final ResourceId thisResourceId = getLuaResource().getId();

            getResourceContext().createAsync(
                rid -> scheduleSuccess(thisResourceId, taskId, rid.asString()),
                throwable -> scheduleFailure(thisResourceId, taskId, throwable),
                module, path, params);

           yieldIndefinitely(luaState);

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
        }

    };

    private JavaFunction invoke = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final ResourceId resourceId = new ResourceId(luaState.checkString(1));
            final String methodName = luaState.checkString(2);
            final Object[] params = luaState.checkJavaObject(3, Object[].class);

            final TaskId taskId = currentTaskId(luaState);
            final ResourceId thisResourceId = getLuaResource().getId();

            getResourceContext().invokeAsync(
                    object -> scheduleSuccess(thisResourceId, taskId, object),
                    throwable -> scheduleFailure(thisResourceId, taskId, throwable),
                    resourceId, methodName, params);

            yieldIndefinitely(luaState);

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
        }

    };

    private JavaFunction invokePath = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final Path path = new Path(luaState.checkString(1));
            final String methodName = luaState.checkString(2);
            final Object[] params = luaState.checkJavaObject(3, Object[].class);

            final TaskId taskId = currentTaskId(luaState);
            final ResourceId thisResourceId = getLuaResource().getId();

            getResourceContext().invokeAsync(
                    object -> scheduleSuccess(thisResourceId, taskId, object),
                    throwable -> scheduleFailure(thisResourceId, taskId, throwable),
                    path, methodName, params);

            yieldIndefinitely(luaState);

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
        }

    };

    private JavaFunction destroy = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final ResourceId resourceId = new ResourceId(luaState.checkString(1));

            final TaskId taskId = currentTaskId(luaState);
            final ResourceId thisResourceId = getLuaResource().getId();

            getResourceContext().destroyAsync(
                    object -> scheduleSuccess(thisResourceId, taskId, null), throwable -> scheduleFailure(thisResourceId, taskId, throwable), resourceId
            );

            yieldIndefinitely(luaState);

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
        }

    };

    private TaskId currentTaskId(final LuaState luaState) {

        luaState.getGlobal(REQUIRE);
        luaState.pushString(CoroutineBuiltin.MODULE_NAME);
        luaState.call(1, 1);
        luaState.getField(-1, CoroutineBuiltin.CURRENT_TASK_ID);

        final String taskId = luaState.toString(-1);
        luaState.pop(1);
        return new TaskId(taskId);

    }

    private void yieldIndefinitely(final LuaState luaState) {

        luaState.getGlobal(REQUIRE);
        luaState.pushString(COROUTINE);
        luaState.call(1, 1);
        luaState.getField(-1, YIELD);
        luaState.pushString(INDEFINITELY.toString());
        luaState.call(1, 0);

    }

    private void scheduleSuccess(final ResourceId resourceId, final TaskId taskId, final Object result) {
        getScheduler().performV(resourceId, resource -> resource.resumeFromNetwork(taskId, result));
    }

    private void scheduleFailure(final ResourceId resourceId, final TaskId taskId, final Throwable throwable) {
        getScheduler().performV(resourceId, resource -> resource.resumeFromNetwork(taskId, throwable));
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
            luaState.newTable();

            // The actual function table
            luaState.newTable();

            luaState.pushJavaFunction(create);
            luaState.setField(-2, CREATE);

            luaState.pushJavaFunction(invoke);
            luaState.setField(-2, INVOKE);

            luaState.pushJavaFunction(invokePath);
            luaState.setField(-2, INVOKE_PATH);

            luaState.pushJavaFunction(invokePath);
            luaState.setField(-2, DESTROY);

            return 1;
        };
    }

    public LuaResource getLuaResource() {
        return luaResource;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }

}
