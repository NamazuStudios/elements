package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import static java.util.UUID.randomUUID;

public class HttpClientIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientIntegrationTest.class);

    private final JettyEmbeddedRESTService jettyEmbeddedRESTService = new JettyEmbeddedRESTService();

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
            .withDefaultHttpClient()
            .withNodeModule(new LuaModule())
            .start();

    private final Node node = getEmbeddedTestService().getNode();

    private final Context context = getEmbeddedTestService().getContext();

    @BeforeClass
    private void start() throws Exception {
        getJettyEmbeddedRESTService().start();
    }

    @AfterClass
    private void stop() throws Exception {
        getNode().stop();
        getJettyEmbeddedRESTService().stop();
    }

    @Test(dataProvider = "resourcesToTest")
    public void performTest(final String methodName) {
        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.http.client", path);
        final String base = getJettyEmbeddedRESTService().getUri();
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName, base);
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

    public Node getNode() {
        return node;
    }

    public Context getContext() {
        return context;
    }

}
