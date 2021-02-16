package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.namazustudios.socialengine.rt.Context.REMOTE;
import static java.util.UUID.randomUUID;

public class HttpClientIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientIntegrationTest.class);

    private final JettyEmbeddedRESTService jettyEmbeddedRESTService = new JettyEmbeddedRESTService()
        .start();

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
            .withWorkerModule(new LuaModule())
            .withWorkerModule(new JavaEventModule())
            .withWorkerModule(new XodusEnvironmentModule().withTempSchedulerEnvironment())
            .withDefaultHttpClient()
        .start();

    private final Context context = getEmbeddedTestService()
        .getClientIocResolver()
        .inject(Context.class, REMOTE);

    @AfterClass
    private void stop() throws Exception {
        getEmbeddedTestService().close();
        getJettyEmbeddedRESTService().stop();
    }

    @Test(dataProvider = "resourcesToTest")
    public void performTest(final String methodName) {
        final var path = new Path(randomUUID().toString());
        final var resourceId = getContext().getResourceContext().create("test.http.client", path);
        final var base = getJettyEmbeddedRESTService().getUri();
        final var result = getContext().getResourceContext().invoke(resourceId, methodName, base);
        logger.info("Successfully got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    @DataProvider
    public static Object[][] resourcesToTest() {
        return new Object[][] {
            {"post"},
            {"get_all"},
            {"get_specific"},
            {"put"},
            {"delete"}
        };
    }

    public JeroMQEmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public JettyEmbeddedRESTService getJettyEmbeddedRESTService() {
        return jettyEmbeddedRESTService;
    }

    public Context getContext() {
        return context;
    }

}
