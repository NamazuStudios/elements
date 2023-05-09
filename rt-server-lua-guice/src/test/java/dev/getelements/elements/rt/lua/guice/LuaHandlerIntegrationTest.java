package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.rt.xodus.XodusEnvironmentModule;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rt.Context.REMOTE;
import static dev.getelements.elements.rt.lua.guice.TestUtils.getUnixFSTest;
import static dev.getelements.elements.rt.lua.guice.TestUtils.getXodusTest;

@Test
public class LuaHandlerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaHandlerIntegrationTest.class);

    @Factory
    public static Object[] getIntegrationTests() {
        return new Object[] {
                getXodusTest(LuaHandlerIntegrationTest::new),
                getUnixFSTest(LuaHandlerIntegrationTest::new)
        };
    }

    private final Context context;

    private final EmbeddedTestService embeddedTestService;

    private LuaHandlerIntegrationTest(final EmbeddedTestService embeddedTestService) {

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

    @Test(dataProvider = "resourcesToTest")
    public void performRetainedHandlerTest(final String moduleName, final String methodName) {

        final var result = getContext()
            .getHandlerContext()
            .invokeRetainedHandler(Attributes.emptyAttributes(), moduleName, methodName);

        logger.info("Successfully got test result {}", result);

    }

    @Test(dataProvider = "resourcesToTest") // TODO Enable This Test
    public void performSingleUseHandlerTest(final String moduleName, final String methodName) {

        final var result = getContext()
            .getHandlerContext()
            .invokeSingleUseHandler(Attributes.emptyAttributes(), moduleName, methodName);

        logger.info("Successfully got test result {}", result);

    }

    @DataProvider
    public static Object[][] resourcesToTest() {
        return LuaResourceIntegrationTest.resourcesToTest();
    }

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public Context getContext() {
        return context;
    }

}
