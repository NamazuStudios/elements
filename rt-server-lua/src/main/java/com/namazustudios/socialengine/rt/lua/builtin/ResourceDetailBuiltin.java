package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.CoroutineBuiltin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.Attributes.emptyAttributes;
import static com.namazustudios.socialengine.rt.lua.Constants.REQUIRE;
import static com.namazustudios.socialengine.rt.lua.builtin.BuiltinUtils.currentTaskId;

/**
 * Provides the details for the resource manipulation operations.
 */
public class ResourceDetailBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDetailBuiltin.class);

    public static final String MODULE_NAME = "namazu.resource.detail";

    public static final String CREATE = "schedule_create";

    public static final String INVOKE = "schedule_invoke";

    public static final String INVOKE_PATH = "schedule_invoke_path";

    public static final String DESTROY = "schedule_destroy";

    private final Context context;

    private final LuaResource luaResource;

    public ResourceDetailBuiltin(final LuaResource luaResource, final Context context) {
        this.context = context;
        this.luaResource = luaResource;
    }

    private JavaFunction create = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final String module = luaState.checkString(1);
            final Path path = new Path(luaState.checkString(2));
            final Map<?, ?> attributesMap = luaState.checkJavaObject(3, Map.class);
            final Object[] params = luaState.checkJavaObject(4, Object[].class);

            final TaskId taskId = currentTaskId(luaState);
            final ResourceId thisResourceId = getLuaResource().getId();

            final Attributes attributes = attributesMap == null ? emptyAttributes() : new SimpleAttributes.Builder()
                .setAttributes(attributesMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue())))
                .build();

            getContext().getResourceContext().createAttributesAsync(
                rid -> getContext().getSchedulerContext().resumeFromNetwork(thisResourceId, taskId, rid.asString()),
                throwable -> getContext().getSchedulerContext().resumeWithError(thisResourceId, taskId, throwable),
                module, path, attributes, params);

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

            getContext().getResourceContext().invokeAsync(
                    object -> getContext().getSchedulerContext().resumeFromNetwork(thisResourceId, taskId, object),
                    throwable -> getContext().getSchedulerContext().resumeWithError(thisResourceId, taskId, throwable),
                resourceId, methodName, params);

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

            getContext().getResourceContext().invokePathAsync(
                    object -> getContext().getSchedulerContext().resumeFromNetwork(thisResourceId, taskId, object),
                    throwable -> getContext().getSchedulerContext().resumeWithError(thisResourceId, taskId, throwable),
                    path, methodName, params);

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

            getContext().getResourceContext().destroyAsync(
                object -> getContext().getSchedulerContext().resumeFromNetwork(thisResourceId, taskId, null),
                throwable -> getContext().getSchedulerContext().resumeWithError(thisResourceId, taskId, throwable),
                resourceId
            );

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
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
        return luaState -> {

            luaState.newTable();

            luaState.pushJavaFunction(create);
            luaState.setField(-2, CREATE);

            luaState.pushJavaFunction(invoke);
            luaState.setField(-2, INVOKE);

            luaState.pushJavaFunction(invokePath);
            luaState.setField(-2, INVOKE_PATH);

            luaState.pushJavaFunction(destroy);
            luaState.setField(-2, DESTROY);

            return 1;
        };
    }

    public Context getContext() {
        return context;
    }

    public LuaResource getLuaResource() {
        return luaResource;
    }

}
