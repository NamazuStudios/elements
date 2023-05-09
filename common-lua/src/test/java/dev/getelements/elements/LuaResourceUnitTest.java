package dev.getelements.elements;

import com.google.inject.Inject;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.id.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static dev.getelements.elements.TestUtils.getUnixFSTest;
import static dev.getelements.elements.TestUtils.getXodusTest;
import static java.util.UUID.randomUUID;

public class LuaResourceUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceUnitTest.class);

    @Factory
    public Object[] getTests() {
        return new Object[] {
                getXodusTest(LuaResourceUnitTest.class),
                getUnixFSTest(LuaResourceUnitTest.class)
        };
    }

    private Context context;

    @Test(dataProvider = "resourcesToTest")
    public void performLuaTest(final String moduleName, final String methodName) {
        final Path path = new Path("socialengine-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(moduleName, path);
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        logger.info("Successfuly got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    @DataProvider
    public static Object[][] resourcesToTest() {
        return new Object[][] {
            {"namazu.elements.test.auth", "test_facebook_security_manifest"},
        };
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

}
