package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static java.util.UUID.randomUUID;

@Guice(modules = LuaResourceIntegrationTest.Module.class)
public class LuaResourceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceIntegrationTest.class);

    private Context context;

    @Test(dataProvider = "resourcesToTest")
    public void performLuaTest(final String moduleName, final String methodName) throws InterruptedException {
        final Path path = new Path("socialengine-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(moduleName, path);
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        logger.info("Successfuly got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    @DataProvider
    public static Object[][] resourcesToTest() {
        return new Object[][] {
            {"namazu.socialengine.test.auth", "test_facebook_security_manifest"},
        };
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
            install(new SimpleContextModule());
            bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(getClass().getClassLoader()));
        }
    }

}
