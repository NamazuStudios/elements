package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.xodus.XodusEnvironmentModule;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.getelements.elements.rt.Context.REMOTE;
import static dev.getelements.elements.rt.Path.fromContextAndComponents;
import static dev.getelements.elements.rt.id.ResourceId.resourceIdFromString;
import static dev.getelements.elements.rt.lua.guice.TestUtils.getUnixFSTest;
import static dev.getelements.elements.rt.lua.guice.TestUtils.getXodusTest;
import static org.testng.Assert.assertEquals;

public class LuaResourceLinkingAdvancedTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLinkingAdvancedTest.class);

    @Factory
    public static Object[] getIntegrationTests() {
        return new Object[] {
                getXodusTest(LuaResourceLinkingAdvancedTest::new),
                getUnixFSTest(LuaResourceLinkingAdvancedTest::new)
        };
    }

    private final Context context;

    private final EmbeddedTestService embeddedTestService;

    private LuaResourceLinkingAdvancedTest(final EmbeddedTestService embeddedTestService) {

        this.embeddedTestService = embeddedTestService;

        final var testApplicationId = getEmbeddedTestService()
                .getWorker()
                .getApplicationId();

        this.context = getEmbeddedTestService()
                .getClient()
                .getContextFactory()
                .getContextForApplication(testApplicationId);

    }

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

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public Context getContext() {
        return context;
    }

}
