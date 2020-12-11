package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.xodus.XodusContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.concurrent.TimeUnit.MINUTES;

public class LuaHandlerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaHandlerIntegrationTest.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
        .withNodeModule(new LuaModule())
        .withNodeModule(new XodusContextModule()
            .withSchedulerThreads(1)
            .withHandlerTimeout(3, MINUTES))
        .withNodeModule(new XodusEnvironmentModule()
            .withTempEnvironments())
        .withNodeModule(new JavaEventModule())
        .withDefaultHttpClient()
        .start();

    private final Node node = getEmbeddedTestService().getNode();

    private final Context context = getEmbeddedTestService().getContext();

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

    @Test(dataProvider = "resourcesToTest")
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
