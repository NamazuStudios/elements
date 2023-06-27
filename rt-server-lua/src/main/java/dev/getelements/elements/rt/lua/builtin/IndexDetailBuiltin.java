package dev.getelements.elements.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.annotation.*;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.id.TaskId;
import dev.getelements.elements.rt.lua.LogAssist;
import dev.getelements.elements.rt.lua.LuaResource;
import dev.getelements.elements.rt.lua.persist.ErisPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.id.ResourceId.resourceIdFromString;
import static dev.getelements.elements.rt.lua.Constants.INDEX_DETAIL_MODULES;
import static dev.getelements.elements.rt.lua.builtin.BuiltinUtils.currentTaskId;

@Intrinsic(
        value = @ModuleDefinition("eci.index"),
        authors = "ptwohig, khudnall",
        summary = "Resource index management system.",
        description = "This API controls the various paths that a resource is indexed to, and provides a means to " +
                "search paths for indices. An index is defined as a link between a resource id and a path. This " +
                "allows for resources to be organized and fetched in a controllable way.",
        methods = {
                @MethodDefinition(
                        value = "list",
                        summary = "Lists all ResourceIds matching a path.",
                        description = "This executes a path query which may accept a wildcard path returning zero or more listings " +
                            "for resources. The return value is a table containing a mapping of path strings to " +
                            "resource_id strings. Care must be taken when passing a path to this function.  The remote " +
                            "will return all paths that match the supplied path.  Therefore, a large query may consume " +
                            "considerable resources. It is recommended that path schemes be crafted to return relatively " +
                            "small data sets.",
                        parameters = {
                                @ParameterDefinition(value="path", type = "string", comment = "The path to search. May be or include a wildcard (\"*\").")
                        },
                        returns = {
                                @ReturnDefinition(comment = "a sequence containing a table containing paths mapped to resource_ids (or an empty table).", type = "table"),
                                @ReturnDefinition(comment = "a response code. See eci.response.code for more details.", type = "number"),
                        }
                ),
                @MethodDefinition(
                        value = "link",
                        summary = "Links a ResourceId to a Path.",
                        description = "Associates a resource id to a path, essentially creating an alias at the new " +
                                "path. There may exist many paths referencing a single resource_id but not the " +
                                "converse. This is useful for generating collections or associations among Resources " +
                                "in the cluster. This function yields until the response is available and must be " +
                                "invoked from within a system-managed coroutine.",
                        parameters = {
                                @ParameterDefinition(value="resource_id", type = "string", comment = "the resource id to link to the new path"),
                                @ParameterDefinition(value="path", type = "string", comment = "the destination path to link")
                        },
                        returns = {
                                @ReturnDefinition(comment = "the response code indicating if the link was successful or not.", type = "number")
                        }
                ),
                @MethodDefinition(
                        value = "link_path",
                        summary = "Links a Path to a Path",
                        description = "Associates a source path to a destination path, essentially creating an alias " +
                                "at the new path. There may exist many. This is useful for generating collections or " +
                                "associations among Resources in the cluster. This function yields until the response " +
                                "is available and must be invoked from within a system-managed coroutine.",
                        parameters = {
                                @ParameterDefinition(value="source", type = "string", comment = "the source path"),
                                @ParameterDefinition(value="destination", type = "string", comment = "the destination path")
                        },
                        returns = {
                                @ReturnDefinition(comment = "the response code indicating if the link was successful or not.", type = "number")
                        }
                ),
                @MethodDefinition(
                        value = "unlink",
                        summary = "Unlinks a path to its associated ResourceId.",
                        description = "Removes a previous association at a specific path. When all paths pointing to " +
                                "a resource are removed, then the cluster will remove and destroy the resource. In " +
                                "this scenario, this will have the same effect as destroying the the resource using " +
                                "its id. This function yields until the response is available and must be invoked " +
                                "from within a system-managed coroutine.",
                        parameters = {
                                @ParameterDefinition(value="path", type = "string", comment = "The path to unlink.")
                        },
                        returns = {
                                @ReturnDefinition(comment = "the affected resource id.", type = "string"),
                                @ReturnDefinition(comment = "a boolean indicating if it was destroyed.", type = "boolean"),
                                @ReturnDefinition(comment = "the actual network response code.", type = "number"),
                        }
                )
        }
)
public class IndexDetailBuiltin implements Builtin {
    private static final Logger logger = LoggerFactory.getLogger(ResourceDetailBuiltin.class);

    public static final String LIST = "schedule_list";

    public static final String LINK = "schedule_link";

    public static final String LINK_PATH = "schedule_link_path";

    public static final String UNLINK = "schedule_unlink";

    private final LuaResource luaResource;

    public IndexDetailBuiltin(final LuaResource luaResource) {
        this.luaResource = luaResource;
    }

    private JavaFunction list = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final Path path = new Path(luaState.checkString(1));

            final TaskId taskId = currentTaskId(luaState);

            final Consumer<List<IndexContext.Listing>> success = listings -> getLuaResource()
                .getLocalContext()
                .getSchedulerContext()
                .resumeFromNetwork(taskId, listings);

            final Consumer<Throwable> failure = throwable -> getLuaResource()
                .getLocalContext()
                .getSchedulerContext()
                .resumeWithError(taskId, throwable);

            getLuaResource()
                .getLocalContextOrContextForPath(path)
                .getIndexContext()
                .listAsync(path, success, failure);

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
        }

    };

    private JavaFunction link = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final ResourceId resourceId = resourceIdFromString(luaState.checkString(1));
            final Path path = new Path(luaState.checkString(2));

            final TaskId taskId = currentTaskId(luaState);

            final Consumer<Void> success = v ->
                getLuaResource()
                    .getLocalContext()
                    .getSchedulerContext()
                    .resumeFromNetwork(taskId, null);

            final Consumer<Throwable> failure = throwable ->
                getLuaResource()
                    .getLocalContext()
                    .getSchedulerContext()
                    .resumeWithError(taskId, throwable);

            getLuaResource()
                .getLocalContextOrContextFor(taskId)
                .getIndexContext()
                .linkAsync(resourceId, path, success, failure);

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
        }

    };

    private JavaFunction linkPath = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final Path source = new Path(luaState.checkString(1));
            final Path destination = new Path(luaState.checkString(2));

            final TaskId taskId = currentTaskId(luaState);

            final Consumer<Void> success = v ->
                getLuaResource()
                    .getLocalContext()
                    .getSchedulerContext()
                    .resumeFromNetwork(taskId, null);

            final Consumer<Throwable> failure = throwable ->
                getLuaResource()
                    .getLocalContext()
                    .getSchedulerContext()
                    .resumeWithError(taskId, throwable);

            getLuaResource()
                .getLocalContext()
                .getIndexContext()
                .linkPathAsync(source, destination, success, failure);

            return 0;

        } catch (Throwable th) {
            logAssist.error("Error invoking method.", th);
            throw th;
        }

    };

    private JavaFunction unlink = luaState -> {

        final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

        try {

            final TaskId taskId = currentTaskId(luaState);
            final Path path = new Path(luaState.checkString(1));

            final Consumer<IndexContext.Unlink> success = u ->
                getLuaResource()
                    .getLocalContext()
                    .getSchedulerContext()
                    .resumeFromNetwork(taskId, u);

            final Consumer<Throwable> failure = throwable ->
                getLuaResource()
                    .getLocalContext()
                    .getSchedulerContext()
                    .resumeWithError(taskId, throwable);

            getLuaResource()
                .getLocalContextOrContextForPath(path)
                .getIndexContext()
                .unlinkAsync(path, success, failure);

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
                return moduleName;
            }

            @Override
            public boolean exists() {
                return INDEX_DETAIL_MODULES.contains(moduleName);
            }
        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {

            luaState.newTable();

            luaState.pushJavaFunction(list);
            luaState.setField(-2, LIST);

            luaState.pushJavaFunction(link);
            luaState.setField(-2, LINK);

            luaState.pushJavaFunction(linkPath);
            luaState.setField(-2, LINK_PATH);

            luaState.pushJavaFunction(unlink);
            luaState.setField(-2, UNLINK);

            return 1;
        };
    }

    @Override
    public void makePersistenceAware(final ErisPersistence erisPersistence) {
        erisPersistence.addPermanentJavaObject(list, IndexDetailBuiltin.class, LIST);
        erisPersistence.addPermanentJavaObject(link, IndexDetailBuiltin.class, LINK);
        erisPersistence.addPermanentJavaObject(linkPath, IndexDetailBuiltin.class, LINK_PATH);
        erisPersistence.addPermanentJavaObject(unlink, IndexDetailBuiltin.class, UNLINK);
    }

    public LuaResource getLuaResource() {
        return luaResource;
    }

}
