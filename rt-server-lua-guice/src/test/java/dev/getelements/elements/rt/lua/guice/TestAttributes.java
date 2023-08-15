package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.test.EmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rt.lua.guice.TestUtils.getUnixFSTest;
import static dev.getelements.elements.rt.lua.guice.TestUtils.getXodusTest;
import static java.util.UUID.randomUUID;

public class TestAttributes {

    private static final Logger logger = LoggerFactory.getLogger(TestAttributes.class);

    private static final Attributes TEST_ATTRIBUTES = new SimpleAttributes.Builder()
            .setAttribute("dev.getelements.foo", "foo")
            .setAttribute("dev.getelements.bar", "bar")
            .setAttribute("dev.getelements.override", "no")
            .build();

    @Factory
    public static Object[] getIntegrationTests() {
        return new Object[] {
                getXodusTest(TestAttributes::new, TEST_ATTRIBUTES),
                getUnixFSTest(TestAttributes::new, TEST_ATTRIBUTES)
        };
    }

    private final Context context;

    private final EmbeddedTestService embeddedTestService;

    private TestAttributes(final EmbeddedTestService embeddedTestService) {

        this.embeddedTestService = embeddedTestService;

        final var testApplicationId = embeddedTestService
                .getWorker()
                .getApplicationId();

        this.context = embeddedTestService
                .getClient()
                .getContextFactory()
                .getContextForApplication(testApplicationId);

    }

    @Test
    public void testDefaultAttributes() {
        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.attributes", path);
        final Object result = getContext().getResourceContext().invoke(resourceId, "check_default_attributes");
        logger.info("Successfully got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    @Test
    public void testOverrideAttributes() {

        final var overrides = new SimpleAttributes.Builder()
                .setAttribute("dev.getelements.foo", "foo")
                .setAttribute("dev.getelements.extra", "yes")
                .setAttribute("dev.getelements.override", "yes")
                .build();

        final Path path = new Path(randomUUID().toString());

        final ResourceId resourceId = getContext()
                .getResourceContext()
                .createAttributes("test.attributes", path, overrides);

        final Object result = getContext().getResourceContext().invoke(resourceId, "check_override_attributes");
        logger.info("Successfully got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);

    }

    @AfterClass
    public void teardown() {
        getEmbeddedTestService().close();
    }

    public Context getContext() {
        return context;
    }

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

}
