package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.id.TaskId;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.persist.ErisPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromString;
import static com.namazustudios.socialengine.rt.lua.builtin.BuiltinUtils.currentTaskId;

public class IndexDetailBuiltin implements Builtin {
    private static final Logger logger = LoggerFactory.getLogger(ResourceDetailBuiltin.class);

    public static final String MODULE_NAME = "namazu.index.detail";

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

            final Consumer<List<IndexContext.Listing>> success = listings ->
                getLuaResource().getLocalContextOrContextFor(taskId)
                .getSchedulerContext()
                .resumeFromNetwork(taskId, listings);

            final Consumer<Throwable> failure = throwable -> getLuaResource().getLocalContextOrContextFor(taskId)
                .getSchedulerContext()
                .resumeWithError(taskId, throwable);

            getLuaResource()
                .getLocalContextOrContextFor(path)
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
                    .getLocalContextOrContextFor(taskId)
                    .getSchedulerContext()
                    .resumeFromNetwork(taskId, null);

            final Consumer<Throwable> failure = throwable ->
                getLuaResource()
                    .getLocalContextOrContextFor(taskId)
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
                        .getLocalContextOrContextFor(taskId)
                        .getSchedulerContext()
                        .resumeFromNetwork(taskId, null);

            final Consumer<Throwable> failure = throwable ->
                    getLuaResource()
                        .getLocalContextOrContextFor(taskId)
                        .getSchedulerContext()
                        .resumeWithError(taskId, throwable);

            getLuaResource()
                .getLocalContextOrContextFor(taskId)
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
                    .getLocalContextOrContextFor(taskId)
                    .getSchedulerContext()
                    .resumeFromNetwork(taskId, u);

            final Consumer<Throwable> failure = throwable ->
                getLuaResource()
                    .getLocalContextOrContextFor(taskId)
                    .getSchedulerContext()
                    .resumeWithError(taskId, throwable);

            getLuaResource()
                .getLocalContextOrContextFor(path)
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
