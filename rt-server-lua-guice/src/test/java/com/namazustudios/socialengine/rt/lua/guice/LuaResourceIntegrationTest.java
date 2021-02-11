package com.namazustudios.socialengine.rt.lua.guice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.jnlua.LuaRuntimeException;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.testng.Assert.*;

/**
 * Provides tests for various lua libraries by instantiating them and invoking specific methods.
 */
public class LuaResourceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceIntegrationTest.class);

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService()
        .withWorkerModule(new LuaModule())
        .withWorkerModule(new XodusEnvironmentModule().withTempSchedulerEnvironment().withTempResourceEnvironment())
        .withDefaultHttpClient()
        .start();

    private final Node node = getEmbeddedTestService().getNode();

    private final Context context = getEmbeddedTestService().getContext();

    @AfterClass
    public void teardown() {
        getEmbeddedTestService().close();
    }

    @SuppressWarnings("Duplicates")
    @Test(dataProvider = "resourcesToTest")
    public void performTest(final String moduleName, final String methodName) {
        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(moduleName, path);
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        logger.info("Successfully got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    @DataProvider
    public static Object[][] resourcesToTest() {
        return new Object[][] {
            {"test.pagination", "test_of"},
            {"test.request", "test_formulate"},
            {"test.request", "test_unpack_headers"},
            {"test.request", "test_unpack_parameters"},
            {"test.request", "test_unpack_path_parameters"},
            {"test.util", "test_uuid"},
            {"test.util.java", "test_pcallx_happy"},
            {"test.util.java", "test_pcallx_handle_exception_1"},
            {"test.util.java", "test_pcallx_handle_exception_2"},
            {"test.util.java", "test_pcallx_handle_exception_3"},
            {"test.resource", "test_create"},
            {"test.resource", "test_invoke"},
            {"test.resource", "test_invoke_fail"},
            {"test.resource", "test_invoke_path"},
            {"test.resource", "test_invoke_path_fail"},
            {"test.resource", "test_invoke_table"},
            {"test.resource", "test_invoke_array"},
            {"test.resource", "test_destroy"},
            {"test.resource", "test_this"},
            {"test.index", "test_list"},
            {"test.index", "test_link"},
            {"test.index", "test_link_path"},
            {"test.index", "test_unlink"},
            {"test.index", "test_unlink_and_destroy"},
            {"test.index", "test_link_yield_and_list"},
            {"test.javamodule", "test_hello_world"},
            {"test.javamodule", "test_return_hello_world"},
            {"test.javamodule", "test_overload_1"},
            {"test.javamodule", "test_overload_2"},
            {"test.response", "test_simple_response"},
            {"test.yield_commit", "test_commit"},
            {"test.yield_commit", "test_complex_commit"},
            {"test.yield_commit", "test_repeat_commit"},
            {"test.pass_table", "pass_simple_table"},
            {"test.pass_table", "pass_simple_array"},
            {"test.pass_table", "pass_complex_array"},
            {"test.pass_table", "pass_complex_table"},
            {"test.pass_table", "pass_complex_table_to_multiple_resources"}
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

    @DataProvider
    public static Object[][] resourcesToTestWithReturnValues() {

        final Function<Consumer<Object>, Consumer<Object>> expected = c -> c;

        return new Object[][] {

            {"test.model", "test_array", expected.apply(result -> assertTrue(result instanceof List, "Expected instance of list."))},
            {"test.model", "test_object", expected.apply(result -> assertTrue(result instanceof Map, "Expected instance of map."))},
            {"test.model", "test_array_default", expected.apply(result -> assertTrue(result instanceof List, "Expected instance of list."))},
            {"test.model", "test_object_default", expected.apply(result -> assertTrue(result instanceof Map, "Expected instance of map."))},
            {"test.model", "test_nil", expected.apply(result -> assertNull(result, "Expected null"))},

            {"test.model", "test_array_remote", expected.apply(result -> assertTrue(result instanceof List, "Expected instance of list."))},
            {"test.model", "test_object_remote", expected.apply(result -> assertTrue(result instanceof Map, "Expected instance of map."))},
            {"test.model", "test_array_default_remote", expected.apply(result -> assertTrue(result instanceof List, "Expected instance of list."))},
            {"test.model", "test_object_default_remote", expected.apply(result -> assertTrue(result instanceof Map, "Expected instance of map."))},
            {"test.model", "test_nil_remote", expected.apply(result -> assertNull(result, "Expected null"))},

        };

    }

    @Test
    public void testRuntimeException() {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.failures", path);

        try {
            getContext().getResourceContext().invoke(resourceId, "lua_runtime_exception");
            fail("Expected exception by this pointl");
        } catch (LuaRuntimeException ex) {
            // Pass Test
            return;
        } catch (Exception ex) {
            fail("Failed with exception.", ex);
        } finally {
            getContext().getResourceContext().destroy(resourceId);
        }

    }

    @Test
    public void testPcallxThrowsUnhandled() {

        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create("test.util.java", path);

        try {
            getContext().getResourceContext().invoke(resourceId, "test_pcallx_unhandled");
            fail("Expected exception by this pointl");
        } catch (LuaRuntimeException ex) {
            // Pass Test
            return;
        } catch (Exception ex) {
            fail("Failed with exception.", ex);
        } finally {
            getContext().getResourceContext().destroy(resourceId);
        }

    }

    @AfterMethod
    public void clearResourceService() {
        try{
            getContext().getResourceContext().destroyAllResources();
        } catch (UnsupportedOperationException ex){

        }
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
