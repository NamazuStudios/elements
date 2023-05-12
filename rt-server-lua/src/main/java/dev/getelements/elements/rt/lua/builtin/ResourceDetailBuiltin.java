package dev.getelements.elements.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.annotation.*;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.id.TaskId;
import dev.getelements.elements.rt.lua.Constants;
import dev.getelements.elements.rt.lua.LogAssist;
import dev.getelements.elements.rt.lua.LuaResource;
import dev.getelements.elements.rt.lua.persist.ErisPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static dev.getelements.elements.rt.Attributes.emptyAttributes;
import static dev.getelements.elements.rt.id.ResourceId.resourceIdFromString;
import static dev.getelements.elements.rt.lua.Constants.RESOURCE_DETAIL_MODULES;
import static dev.getelements.elements.rt.lua.builtin.BuiltinUtils.currentTaskId;
import static java.util.stream.Collectors.toMap;

@Intrinsic(
        value = @ModuleDefinition("eci.index"),
        authors = "ptwohig, khudnall",
        summary = "Resource index management system.",
        description = "This API controls the various paths that a resource is indexed to, and provides a means to " +
                "search paths for indices. An index is defined as a link between a resource id and a path. This " +
                "allows for resources to be organized and fetched in a controllable way.",
        methods = {
                @MethodDefinition(
                        value = "create",
                        summary = "Creates a resource at the supplied path.",
                        description =
                                "This will create a resource at the supplied path, specifying the module as well as " +
                                "any additional parameters which will be read by the remote resource.  This returns " +
                                "both the network response as well as the resource ID. This function yields until " +
                                "the response is available and must be invoked from within a system-managed coroutine.",
                        parameters = {
                                @ParameterDefinition(value="module", type = "string", comment = "the module name"),
                                @ParameterDefinition(value="path", type = "string", comment = "the initial path of the module"),
                                @ParameterDefinition(value="attributes", type = "string", comment = "the resource attributes")
                        },
                        returns = {
                                @ReturnDefinition(comment = "the resource id", type = "string"),
                                @ReturnDefinition(comment = "the response code", type = "number"),
                        }
                ),
                @MethodDefinition(
                        value = "invoke",
                        summary = "Invokes a method (possibly remotely) on the supplied resource.",
                        description =
                                "This will invoke, possibly remotely, a method on the supplied resource_id. All " +
                                "arguments passed to the method are handed through the variadic arguments. This " +
                                "function yields until the response is available and must be invoked from within a " +
                                "system-managed coroutine.",
                        parameters = {
                                @ParameterDefinition(value="resource_id", type = "string", comment = "the resource id to link to the new path"),
                                @ParameterDefinition(value="method", type = "string", comment = "the name of the remote method"),
                                @ParameterDefinition(value="arguments", type = "...", comment = "any arguments to pass to the method"),
                        },
                        returns = {
                                @ReturnDefinition(comment = "the response", type = "object"),
                                @ReturnDefinition(comment = "the response code", type = "number"),
                        }
                ),
                @MethodDefinition(
                        value = "invoke_path",
                        summary = "Invokes a method (possibly remotely) on the supplied resource.",
                        description = "This will invoke, possibly remotely, a method on the supplied path. All " +
                                "arguments passed to the method are handed through the variadic arguments. This " +
                                "function yields until the response is available and must be invoked from within a " +
                                "system-managed coroutine.",
                        parameters = {
                                @ParameterDefinition(value="path", type = "string", comment = "the initial path of the module"),
                                @ParameterDefinition(value="method", type = "string", comment = "the name of the remote method"),
                                @ParameterDefinition(value="arguments", type = "...", comment = "any arguments to pass to the method"),
                        },
                        returns = {
                                @ReturnDefinition(comment = "the response", type = "object"),
                                @ReturnDefinition(comment = "the response code", type = "number"),
                        }
                ),
                @MethodDefinition(
                        value = "destroy",
                        summary = "Destroys a resource (possibly remotely).",
                        description = "This will permanently destroy and unlink the resource with the supplied ID. " +
                                "Once destroyed, the resource will no longer accept method requests, and will no " +
                                "longer be indexable. This function yields until the response is available and must " +
                                "be invoked from within a system-managed coroutine.",
                        parameters = {
                                @ParameterDefinition(value="resource_id", type = "string", comment = "The id of the resource to destroy.")
                        },
                        returns = {
                                @ReturnDefinition(comment = "the resource_id of the destroyed resource", type = "string"),
                                @ReturnDefinition(comment = "the response code", type = "number"),
                        }
                )
        }
)
public class ResourceDetailBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDetailBuiltin.class);

    public static final String CREATE = "schedule_create";

    public static final String INVOKE = "schedule_invoke";

    public static final String INVOKE_PATH = "schedule_invoke_path";

    public static final String DESTROY = "schedule_destroy";

    private final LuaResource luaResource;

    public ResourceDetailBuiltin(final LuaResource luaResource) {
        this.luaResource = luaResource;
    }

    private JavaFunction create = luaState -> {

        final TaskId taskId = currentTaskId(luaState);
        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final String module = luaState.checkString(1);
            final Path path = new Path(luaState.checkString(2));
            final Map<?, ?> attributesMap = luaState.checkJavaObject(3, Map.class);
            final Object[] params = luaState.checkJavaObject(4, Object[].class);

            final Attributes attributes = attributesMap == null ? emptyAttributes() : new SimpleAttributes.Builder()
                .setAttributes(attributesMap.entrySet().stream().collect(toMap(e -> e.getKey().toString(), Map.Entry::getValue)))
                .build();

            getLuaResource().getLocalContextOrContextForPath(path).getResourceContext().createAttributesAsync(
                rid -> getLuaResource().getLocalContext().getSchedulerContext().resumeFromNetwork(taskId, rid.asString()),
                throwable -> getLuaResource().getLocalContext().getSchedulerContext().resumeWithError(taskId, throwable),
                module, path, attributes, params);

            return 0;

        } catch (Exception ex) {
            logAssist.error("Error invoking method.", ex);
            getLuaResource().getLocalContext().getTaskContext().finishWithError(taskId, ex);
            throw ex;
        }

    };

    private JavaFunction invoke = luaState -> {

        final TaskId taskId = currentTaskId(luaState);
        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final ResourceId resourceId = resourceIdFromString(luaState.checkString(1));
            final String methodName = luaState.checkString(2);
            final Object[] params = luaState.checkJavaObject(3, Object[].class);

            logger.trace("Invoking {} {} {}", resourceId, methodName, params);

            getLuaResource().getLocalContextOrContextFor(resourceId).getResourceContext().invokeAsync(
                object -> getLuaResource().getLocalContext().getSchedulerContext().resumeFromNetwork(taskId, object),
                throwable -> getLuaResource().getLocalContext().getSchedulerContext().resumeWithError(taskId, throwable),
                resourceId, methodName, params);

            return 0;

        } catch (Exception ex) {
            logAssist.error("Error invoking method.", ex);
            getLuaResource().getLocalContext().getTaskContext().finishWithError(taskId, ex);
            throw ex;
        }

    };

    private JavaFunction invokePath = luaState -> {

        final TaskId taskId = currentTaskId(luaState);
        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final Path path = new Path(luaState.checkString(1));
            final String methodName = luaState.checkString(2);
            final Object[] params = luaState.checkJavaObject(3, Object[].class);

            getLuaResource().getLocalContextOrContextForPath(path).getResourceContext().invokePathAsync(
                object -> getLuaResource().getLocalContext().getSchedulerContext().resumeFromNetwork(taskId, object),
                throwable -> getLuaResource().getLocalContext().getSchedulerContext().resumeWithError(taskId, throwable),
                path, methodName, params);

            return 0;

        } catch (Exception ex) {
            logAssist.error("Error invoking method.", ex);
            getLuaResource().getLocalContext().getTaskContext().finishWithError(taskId, ex);
            throw ex;
        }

    };

    private JavaFunction destroy = luaState -> {

        final TaskId taskId = currentTaskId(luaState);
        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final ResourceId resourceId = resourceIdFromString(luaState.checkString(1));

            getLuaResource().getLocalContextOrContextFor(resourceId).getResourceContext().destroyAsync(
                object -> {
                    if (taskId.getResourceId().equals(resourceId)) {
                        logger.info("Destroyed {}", resourceId);
                    } else {
                        getLuaResource()
                            .getLocalContext()
                            .getSchedulerContext()
                            .resumeFromNetwork(taskId, null);
                    }
                },
                throwable -> {

                    if (taskId.getResourceId().equals(resourceId)) {
                        logger.error("Could not self-destruct resource {}", resourceId);
                    }

                    getLuaResource()
                        .getLocalContext()
                        .getSchedulerContext().resumeWithError(taskId, throwable);

                },
                resourceId
            );

            return 0;

        } catch (Exception ex) {
            logAssist.error("Error invoking method.", ex);
            getLuaResource().getLocalContext().getTaskContext().finishWithError(taskId, ex);
            throw ex;
        }

    };

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {
            @Override
            public String getChunkName() {
                return moduleName;
            }

            @Override
            public boolean exists() {
                return RESOURCE_DETAIL_MODULES.contains(moduleName);
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

    @Override
    public void makePersistenceAware(final ErisPersistence erisPersistence) {
        erisPersistence.addPermanentJavaObject(create, IndexDetailBuiltin.class, CREATE);
        erisPersistence.addPermanentJavaObject(invoke, IndexDetailBuiltin.class, INVOKE);
        erisPersistence.addPermanentJavaObject(invokePath, IndexDetailBuiltin.class, INVOKE_PATH);
        erisPersistence.addPermanentJavaObject(destroy, IndexDetailBuiltin.class, DESTROY);
    }

    public LuaResource getLuaResource() {
        return luaResource;
    }

}
