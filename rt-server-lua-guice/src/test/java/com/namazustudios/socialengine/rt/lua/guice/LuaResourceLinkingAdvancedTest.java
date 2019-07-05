package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.xodus.XodusContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.testng.Assert.assertEquals;

public class LuaResourceLinkingAdvancedTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLinkingAdvancedTest.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
            .withNodeModule(new LuaModule())
            .withNodeModule(new XodusContextModule()
                .withSchedulerThreads(1)
                .withHandlerTimeout(3, MINUTES))
            .withNodeModule(new XodusEnvironmentModule()
                .withTempEnvironments())
            .withDefaultHttpClient()
        .start();

    private final Node node = getEmbeddedTestService().getNode();

    private final Context context = getEmbeddedTestService().getContext();

    @AfterClass
    public void teardown() {
        getEmbeddedTestService().close();
    }

    @Test()
    public void performAdvancedLinkingTest() throws Exception {

        final String pathASuffix = UUID.randomUUID().toString();
        final String pathBSuffix = UUID.randomUUID().toString();

        final List<String> results = (List<String>) getContext()
            .getHandlerContext()
            .invokeSingleUseHandler(
                Attributes.emptyAttributes(),
                "test.link.handler", "test_create", pathASuffix, pathBSuffix);

        final Path root = new Path(results.get(0));
        final ResourceId resourceId = new ResourceId(results.get(1));

        final Map<String, String> resultsA = (Map<String, String>) getContext()
            .getHandlerContext()
            .invokeSingleUseHandler(
                Attributes.emptyAttributes(),
                "test.link.handler", "test_list", pathASuffix);

        final Map<String, String> resultsB = (Map<String, String>) getContext()
            .getHandlerContext()
            .invokeSingleUseHandler(
                Attributes.emptyAttributes(),
                "test.link.handler", "test_list", pathBSuffix);

        logger.info("ResultsA {}.  ResultsB {}", resultsA, resultsB);

        assertEquals(resultsA.size(), 1);
        assertEquals(resultsB.size(), 1);

        final Path patha = Path.fromComponents("test_case", pathASuffix, resourceId.toString());
        final Path pathb = Path.fromComponents("test_case", pathBSuffix, resourceId.toString());

        assertEquals(resultsA.get(patha.toNormalizedPathString()), resourceId.toString());
        assertEquals(resultsB.get(pathb.toNormalizedPathString()), resourceId.toString());

    }


    public JeroMQEmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public Node getNode() {
        return node;
    }

    public Context getContext() {
        return context;
    }

}
