package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.util.SyncWait;
import com.namazustudios.socialengine.rt.xodus.XodusContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.testng.Assert.assertEquals;

public class TestYielding {

    private static final Logger logger = LoggerFactory.getLogger(TestYielding.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
        .withWorkerModule(new LuaModule())
        .withDefaultHttpClient()
        .withWorkerModule(new XodusContextModule()
            .withSchedulerThreads(1)
            .withHandlerTimeout(3, MINUTES))
        .withWorkerModule(new XodusEnvironmentModule()
            .withTempEnvironments())
        .start();

    private final Context context = embeddedTestService.getContext();

    @Test
    public void testConcurrentCoroutine() throws Exception {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.concurrent_coroutine", path);

        final SyncWait<Object> blockSyncWait = new SyncWait<>(getClass());
        logger.info("Starting blocking co-routine.");

        getContext()
            .getResourceContext()
            .invokeAsync(
                blockSyncWait.getResultConsumer(),
                blockSyncWait.getErrorConsumer(),
                resourceId, "block");

        final SyncWait<Object> awakeSyncWait = new SyncWait<>(getClass());
        logger.info("Awaking Coroutine.");

        getContext()
            .getResourceContext()
            .invokeAsync(
                awakeSyncWait.getResultConsumer(),
                awakeSyncWait.getErrorConsumer(),
                resourceId, "awake");

        assertEquals(blockSyncWait.get(), "OK");
        assertEquals(awakeSyncWait.get(), "OK");

    }

    @Test
    public void testShortYield() throws Exception {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.concurrent_coroutine", path);

        logger.info("Starting blocking co-routine.");

        final SyncWait<Object> shortYieldSyncWait = new SyncWait<>(getClass());

        getContext()
            .getResourceContext()
            .invokeAsync(
                shortYieldSyncWait.getResultConsumer(),
                shortYieldSyncWait.getErrorConsumer(),
                resourceId, "short_yield");

        assertEquals(shortYieldSyncWait.get(), "OK");

    }

    @Test
    public void testKillAndRestart() throws Exception {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.restarting_coroutine", path);

        final SyncWait<Object> startSyncWait = new SyncWait<>(getClass());

        getContext()
            .getResourceContext()
            .invokeAsync(
                startSyncWait.getResultConsumer(),
                startSyncWait.getErrorConsumer(),
                resourceId, "start");

        final SyncWait<Object> resumeSyncWait = new SyncWait<>(getClass());

        getContext()
            .getResourceContext()
            .invokeAsync(
                resumeSyncWait.getResultConsumer(),
                resumeSyncWait.getErrorConsumer(),
                resourceId, "resume");

        startSyncWait.get();
        resumeSyncWait.get();

    }

    public Context getContext() {
        return context;
    }

}
