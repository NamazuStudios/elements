package com.namazustudios.socialengine.rt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.jnlua.LuaRuntimeException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.guice.*;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.IoCInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ClusterClientContextModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zeromq.ZContext;

import javax.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.zeromq.ZContext.shadow;

/**
 * Provides tests for various lua libraries by instantiating them and invoking specific methods.
 */
public class LuaResourceIntegrationTest {

    private static final String INTERNAL_NODE_ADDRESS = "inproc://integration-test";

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceIntegrationTest.class);

    private Node node;

    private Context context;

    @BeforeClass
    public void setup() {

        final ZContext zContext = new ZContext();
        final Injector nodeInjector = Guice.createInjector(new NodeModule(shadow(zContext)));
        final Injector clientInjector = Guice.createInjector(new ClientModule(shadow(zContext)));

        setNode(nodeInjector.getInstance(Node.class));
        setContext(clientInjector.getInstance(Context.class));

        getNode().start();
        getContext().start();
    }

    @AfterClass
    public void teardown() {
        getNode().stop();
        getContext().shutdown();
    }

    @Test(dataProvider = "resourcesToTest")
    public void performTest(final String moduleName, final String methodName) throws InterruptedException {
        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(moduleName, path);
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        logger.info("Successfuly got test result {}", result);
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
            {"test.resource", "test_create"},
            {"test.resource", "test_invoke"},
            {"test.resource", "test_invoke_fail"},
            {"test.resource", "test_invoke_path"},
            {"test.resource", "test_invoke_path_fail"},
            {"test.resource", "test_invoke_table"},
            {"test.resource", "test_invoke_array"},
            {"test.resource", "test_destroy"},
            {"test.index", "test_list"},
            {"test.index", "test_link"},
            {"test.index", "test_link_path"},
            {"test.index", "test_unlink"},
            {"test.index", "test_unlink_and_destroy"},
            {"test.box2d", "test_hello_world"},
            {"test.javamodule", "test_hello_world"},
            {"test.javamodule", "test_return_hello_world"},
            {"test.javamodule", "test_overload_1"},
            {"test.javamodule", "test_overload_2"},
            {"test.response", "test_simple_response"}
        };
    }

    @Test(dataProvider = "resourcesToTestWithReturnValues")
    public void performTestWithReturnValue(final String moduleName,
                                           final String methodName,
                                           final Consumer<Object> resultConsumer) throws InterruptedException, Exception {
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
        } catch (InternalException ex) {
            assertTrue((ex.getCause() instanceof LuaRuntimeException), "Expected cause to be LuaRuntimeException.");
        } finally {
            getContext().getResourceContext().destroy(resourceId);
        }

    }

    @AfterMethod
    public void clearResourceService() {
        getContext().getResourceContext().destroyAllResources();
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

    public Node getNode() {
        return node;
    }

    @Inject
    public void setNode(Node node) {
        this.node = node;
    }

    public static class ClientModule extends PrivateModule {

        private final ZContext context;

        public ClientModule(ZContext context) {
            this.context = context;
        }

        @Override
        protected void configure() {
            expose(Context.class);
            bind(ZContext.class).toInstance(context);
            install(new ClusterClientContextModule());
            install(new JeroMQRemoteInvokerModule()
                .withConnectAddress(INTERNAL_NODE_ADDRESS)
                .withMinimumConnections(5)
                .withTimeout(60));
        }

    }

    public static class NodeModule extends PrivateModule {

        private final ZContext context;

        public NodeModule(ZContext context) {
            this.context = context;
        }

        @Override
        protected void configure() {

            expose(Node.class);
            expose(ResourceContext.class);
            expose(IndexContext.class);
            expose(SchedulerContext.class);

            bind(ZContext.class).toInstance(context);
            bind(InvocationDispatcher.class).to(IoCInvocationDispatcher.class);
            bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(getClass().getClassLoader()));
            bind(Context.class).to(SimpleContext.class).asEagerSingleton();

            install(new LuaModule() {
                @Override
                protected void configureFeatures() {
                    super.configureFeatures();
                    bindBuiltin(TestJavaModule.class).toModuleNamed("test.java.module");
                }
            });

            install(new SimpleServicesModule());
            install(new SimpleResourceContextModule());
            install(new SimpleIndexContextModule());
            install(new SimpleSchedulerContextModule());

            install(new GuiceIoCResolverModule());
            install(new JeroMQNodeModule()
                .withBindAddress(INTERNAL_NODE_ADDRESS)
                .withNodeId("integration-test-node")
                .withNodeName("integration-test-node")
                .withMinimumConnections(5)
                .withTimeout(60)
                .withNumberOfDispatchers(10));

        }

    }

}
