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
import org.testng.annotations.Test;

import static com.namazustudios.socialengine.rt.Context.REMOTE;

public class LuaHandlerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaHandlerIntegrationTest.class);

    private final EmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
        .withClient()
        .withApplicationNode()
            .withNodeModules(new LuaModule())
            .withNodeModules(new JavaEventModule())
            .withNodeModules(new ClasspathAssetLoaderModule().withDefaultPackageRoot())
        .endApplication()
        .withWorkerModule(new XodusEnvironmentModule().withTempSchedulerEnvironment())
        .withDefaultHttpClient()
        .start();

    private final ApplicationId testApplicationId = getEmbeddedTestService()
        .getWorker()
        .getApplicationId();

    private final Context context = getEmbeddedTestService()
        .getClient()
        .getContextFactory()
        .getContextForApplication(testApplicationId);

    @AfterClass
    public void teardown() {
        getEmbeddedTestService().close();
    }

//    @Test(dataProvider = "resourcesToTest")
    @Test(dataProvider = "resourcesToTest", threadPoolSize = 5, invocationCount = 10)
    public void performRetainedHandlerTest(final String moduleName, final String methodName) {

        final Object result = getContext()
            .getHandlerContext()
            .invokeRetainedHandler(Attributes.emptyAttributes(), moduleName, methodName);

        logger.info("Successfully got test result {}", result);

    }

    @Test(dataProvider = "resourcesToTest", enabled = false) // TODO Enable This Test
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
