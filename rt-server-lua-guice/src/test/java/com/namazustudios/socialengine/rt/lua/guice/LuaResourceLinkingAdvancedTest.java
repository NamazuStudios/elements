package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.Path.fromContextAndComponents;
import static com.namazustudios.socialengine.rt.id.ResourceId.resourceIdFromString;
import static org.testng.Assert.assertEquals;

public class LuaResourceLinkingAdvancedTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLinkingAdvancedTest.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
            .withWorkerModule(new LuaModule())
            .withWorkerModule(new XodusEnvironmentModule().withTempSchedulerEnvironment().withTempResourceEnvironment())
            .withDefaultHttpClient()
        .start();

    private final Context context = getEmbeddedTestService().getContext();

    @AfterClass
    public void teardown() {
        getEmbeddedTestService().close();
    }

    @Test
    public void performAdvancedLinkingTest() {

        final var pathASuffix = UUID.randomUUID().toString();
        final var pathBSuffix = UUID.randomUUID().toString();

        final var results = (List<String>) getContext()
            .getHandlerContext()
            .invokeSingleUseHandler(
                Attributes.emptyAttributes(),
                "test.link.handler", "test_create", pathASuffix, pathBSuffix);

        final var resourceId = resourceIdFromString(results.get(1));

        final var resultsA = (Map<String, String>) getContext()
            .getHandlerContext()
            .invokeSingleUseHandler(
                Attributes.emptyAttributes(),
                "test.link.handler", "test_list", pathASuffix);

        final var resultsB = (Map<String, String>) getContext()
            .getHandlerContext()
            .invokeSingleUseHandler(
                Attributes.emptyAttributes(),
                "test.link.handler", "test_list", pathBSuffix);

        logger.info("ResultsA {}.  ResultsB {}", resultsA, resultsB);

        assertEquals(resultsA.size(), 1);
        assertEquals(resultsB.size(), 1);

        final var patha = fromContextAndComponents(resourceId, "test_case", pathASuffix, resourceId.toString());
        final var pathb = fromContextAndComponents(resourceId, "test_case", pathBSuffix, resourceId.toString());

        assertEquals(resultsA.get(patha.toNormalizedPathString()), resourceId.toString());
        assertEquals(resultsB.get(pathb.toNormalizedPathString()), resourceId.toString());

    }

    public JeroMQEmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public Context getContext() {
        return context;
    }

}
