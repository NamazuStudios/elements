package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.Path;
import dev.getelements.elements.test.EmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rt.lua.guice.TestUtils.getUnixFSTest;
import static dev.getelements.elements.rt.lua.guice.TestUtils.getXodusTest;
import static java.util.UUID.randomUUID;

@Test
public class HttpClientIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientIntegrationTest.class);

    @Factory
    public static Object[] getIntegrationTests() {
        return new Object[] {
            getXodusTest(HttpClientIntegrationTest::new),
            getUnixFSTest(HttpClientIntegrationTest::new)
        };
    }

    private final JettyEmbeddedRESTService jettyEmbeddedRESTService = new JettyEmbeddedRESTService()
        .start();

    private final Context context;

    private final EmbeddedTestService embeddedTestService;

    private HttpClientIntegrationTest(final EmbeddedTestService embeddedTestService) {

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
            { "post" },
            { "post_multipart" },
            { "get_all" },
            { "get_all_query" },
            { "get_specific" },
            { "put" },
            { "delete" }
        };
    }

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public JettyEmbeddedRESTService getJettyEmbeddedRESTService() {
        return jettyEmbeddedRESTService;
    }

    public Context getContext() {
        return context;
    }

}
