package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.concurrent.Future;

import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;

public class TestYielding {

    private static final Logger logger = LoggerFactory.getLogger(TestYielding.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService().start();

    private final Context context = embeddedTestService.getContext();

    @Test
    public void testConcurrentCoroutine() throws Exception {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.concurrent_coroutine", path);

        logger.info("Starting blocking co-routine.");

        final Future<Object> blockedFuture = getContext()
            .getResourceContext()
            .invokeAsync(
                r -> logger.info("Block Success: {}", r),
                ex -> logger.error("Failure", ex),
                resourceId, "block");

        logger.info("Awaking Coroutine.");
        final Future<Object> awokeFuture = getContext()
            .getResourceContext()
            .invokeAsync(
                r -> logger.info("Awake Success: {}", r),
                ex -> logger.error("Failure", ex),
                resourceId, "awake");

        assertEquals(blockedFuture.get(), "OK");
        assertEquals(awokeFuture.get(), "OK");

    }

    @Test
    public void testShortYield() throws Exception {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.concurrent_coroutine", path);

        logger.info("Starting blocking co-routine.");

        final Future<Object> shortYieldFuture = getContext()
                .getResourceContext()
                .invokeAsync(
                        r -> logger.info("Success: {}", r),
                        ex -> logger.error("Failure", ex),
                        resourceId, "short_yield");

        assertEquals(shortYieldFuture.get(), "OK");

    }

    public Context getContext() {
        return context;
    }

}
