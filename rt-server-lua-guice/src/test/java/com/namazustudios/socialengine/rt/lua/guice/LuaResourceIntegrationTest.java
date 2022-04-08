package com.namazustudios.socialengine.rt.lua.guice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.jnlua.LuaRuntimeException;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.lua.guice.TestUtils.getUnixFSTest;
import static com.namazustudios.socialengine.rt.lua.guice.TestUtils.getXodusTest;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

/**
 * Provides tests for various lua libraries by instantiating them and invoking specific methods.
 */
public class LuaResourceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceIntegrationTest.class);

    @Factory
    public static Object[] getIntegrationTests() {
        return new Object[] {
            getXodusTest(LuaResourceIntegrationTest::new),
//            getUnixFSTest(LuaResourceIntegrationTest::new)
        };
    }

    private final Context context;

    private final EmbeddedTestService embeddedTestService;

    private LuaResourceIntegrationTest(final EmbeddedTestService embeddedTestService) {

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
            {"test.java", "test_pcallx_happy"},
            {"test.java", "test_pcallx_handle_exception_1"},
            {"test.java", "test_pcallx_handle_exception_2"},
            {"test.java", "test_pcallx_handle_exception_3"},
            {"test.resource", "test_create"},
            {"test.resource", "test_invoke"},
            {"test.resource", "test_invoke_fail"},
            {"test.resource", "test_invoke_path"},
            {"test.resource", "test_invoke_path_fail"},
            {"test.resource", "test_invoke_table"},
            {"test.resource", "test_invoke_array"},
            {"test.resource", "test_destroy"},
            {"test.resource", "test_this"},
            {"test.index", "test_list_local"},
            {"test.index", "test_list_remote"},
            {"test.index", "test_list_wildcard"},
            {"test.index", "test_link_local"},
            {"test.index", "test_link_remote"},
            {"test.index", "test_link_path_local"},
            {"test.index", "test_link_path_remote"},
            {"test.index", "test_unlink_local"},
            {"test.index", "test_unlink_remote"},
            {"test.index", "test_unlink_and_destroy_local"},
            {"test.index", "test_unlink_and_destroy_remote"},
            {"test.index", "test_link_yield_and_list_local"},
            {"test.index", "test_link_yield_and_list_remote"},
            {"test.javamodule", "test_hello_world"},
            {"test.javamodule", "test_return_hello_world"},
            {"test.javamodule", "test_overload_1"},
            {"test.javamodule", "test_overload_2"},
            {"test.javamodule", "test_overload_fail"},
            {"test.javamodule", "test_java_pcall"},
            {"test.response", "test_simple_response"},
            {"test.yield_commit", "test_commit"},
            {"test.yield_commit", "test_complex_commit"},
            {"test.yield_commit", "test_repeat_commit"},
            {"test.pass_table", "pass_simple_table"},
            {"test.pass_table", "pass_simple_array"},
            {"test.pass_table", "pass_complex_array"},
            {"test.pass_table", "pass_complex_table"},
            {"test.pass_table", "pass_complex_table_to_multiple_resources"},
            {"test.proxy", "test_create"},
            {"test.proxy", "test_invoke"},
            {"test.proxy", "test_invoke_path"},
            {"test.proxy", "test_list"},
            {"test.runtime", "test_instance_id"},
            {"test.runtime", "test_application_id"},
            {"test.runtime", "test_node_id"},
            {"test.runtime", "test_resource_id"},
            {"test.runtime", "test_node_id_from_resource_id"},
            {"test.cluster", "test_get_node_ids"},
            {"test.cluster", "test_get_best_node_id_for_application"},
            {"test.cluster", "test_get_best_node_id_for_application_id"},
            {"test.cluster", "test_get_node_ids_for_application"},
            {"test.cluster", "test_get_node_ids_for_application_id"},
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
        final ResourceId resourceId = getContext().getResourceContext().create("test.java", path);

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

//    @AfterMethod
//    public void clearResourceService() {
//        try{
//            getContext().getResourceContext().destroyAllResources();
//        } catch (UnsupportedOperationException ex){
//
//        }
//    }

    public EmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

    public Context getContext() {
        return context;
    }

}
