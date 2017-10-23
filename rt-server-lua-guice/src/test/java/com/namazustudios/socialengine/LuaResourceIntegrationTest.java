package com.namazustudios.socialengine;


import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.SimpleServicesModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * Provides tests for various lua libraries by instantiating them and invoking specific methods.
 */
@Guice(modules = LuaResourceIntegrationTest.Module.class)
public class LuaResourceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceIntegrationTest.class);

    private ResourceLoader resourceLoader;

    @Test(dataProvider = "resourcesToTest")
    public void performTest(final String moduleName, final String methodName) {
        try (final Resource resource = getResourceLoader().load(moduleName)) {

            resource.getMethodDispatcher(methodName)
                .params()
                .dispatch(o -> logger.info("Got result from invocation {}"),
                          th -> Assert.fail("Caught exception in Lua code.", th));
        }

    }

    @DataProvider
    public static Object[][] resourcesToTest() {
        return new Object[][] {
            {"test.pagination", "test_of"},
            {"test.request", "test_formulate"},
            {"test.request", "test_unpack_headers"},
            {"test.request", "test_unpack_parameters"},
            {"test.request", "test_unpack_path_parameters"},
            {"test.resource", "test_invoke"},
            {"test.util", "test_uuid"}
        };
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
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
