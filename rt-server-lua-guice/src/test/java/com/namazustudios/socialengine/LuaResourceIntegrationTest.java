package com.namazustudios.socialengine;


import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.SimpleServicesModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Provides tests for various lua libraries by instantiating them and invoking specific methods.
 */
@Guice(modules = LuaResourceIntegrationTest.Module.class)
public class LuaResourceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceIntegrationTest.class);

    private Context context;

    private ResourceService resourceService;

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
            {"test.resource", "test_destroy"},
            {"test.index", "test_list"},
            {"test.index", "test_link"},
            {"test.index", "test_link_path"},
            {"test.index", "test_unlink"},
            {"test.index", "test_unlink_and_destroy"}
        };
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

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            install(new LuaModule());
            install(new SimpleServicesModule());
            bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(getClass().getClassLoader()));
        }

    }

}
