package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.guice.ClasspathAssetLoaderModule;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static com.namazustudios.socialengine.rt.Context.REMOTE;
import static com.namazustudios.socialengine.rt.lua.guice.TestUtils.getUnixFSTest;
import static com.namazustudios.socialengine.rt.lua.guice.TestUtils.getXodusTest;

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

        final Object result = getContext()
            .getHandlerContext()
            .invokeRetainedHandler(Attributes.emptyAttributes(), moduleName, methodName);

        logger.info("Successfully got test result {}", result);

    }

    @Test(dataProvider = "resourcesToTest") // TODO Enable This Test
    public void performSingleUseHandlerTest(final String moduleName, final String methodName) {

        final Object result = getContext()
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
