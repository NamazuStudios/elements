package com.namazustudios.socialengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.namazustudios.socialengine.TestUtils.getUnixFSIntegrationTest;
import static com.namazustudios.socialengine.TestUtils.getXodusIntegrationTest;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertNull;

public class LuaMongoIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaMongoIntegrationTest.class);

    private Context context;

    private Application application;

    private EmbeddedTestService embeddedTestService;


    @Factory
    public static Object[] getTests() {
        return new Object[] {
                getXodusIntegrationTest(LuaMongoIntegrationTest.class),
                getUnixFSIntegrationTest(LuaMongoIntegrationTest.class)
        };
    }

    @AfterClass
    public void teardown() {
        getEmbeddedTestService().close();
    }

    @DataProvider
    public static Object[][] resourcesToTestWithReturnValues() {

        final Function<Consumer<Object>, Consumer<Object>> expected = c -> c;

        return new Object[][] {
                {"namazu.elements.test.mongodb", "test_get_elements_database", expected.apply(result -> assertNull(result, "Expected null"))},
                {"namazu.elements.test.mongodb", "test_get_application_collection", expected.apply(result -> assertNull(result, "Expected null"))},
                {"namazu.elements.test.mongodb", "test_get_users_collection", expected.apply(result -> assertNull(result, "Expected null"))},
                {"namazu.elements.test.mongodb", "test_create_application_entries", expected.apply(result -> assertNull(result, "Expected null"))},
                {"namazu.elements.test.mongodb", "test_modify_application_entries", expected.apply(result -> assertNull(result, "Expected null"))},
                {"namazu.elements.test.mongodb", "test_delete_application_entries", expected.apply(result -> assertNull(result, "Expected null"))},
        };

    }

    @Test(dataProvider = "resourcesToTestWithReturnValues")
    public void performTestWithReturnValue(final String moduleName,
                                           final String methodName,
                                           final Consumer<Object> resultConsumer) throws Exception {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(moduleName, path);
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        final ObjectMapper om = new ObjectMapper();
        final String value = om.writeValueAsString(result);

        logger.info("Successfuly got test result {}", result);

        resultConsumer.accept(result);
        getContext().getResourceContext().destroy(resourceId);
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

    public Application getApplication() {
        return application;
    }

    @Inject
    public void setApplication(Application application) {
        this.application = application;
    }

    @Inject
    public void setEmbeddedTestService(EmbeddedTestService embeddedTestService) {
        this.embeddedTestService = embeddedTestService;
    }

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }
}
