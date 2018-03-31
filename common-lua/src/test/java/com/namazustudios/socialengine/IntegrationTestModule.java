package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ClasspathAssetLoader;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.lua.guice.JeroMQEmbeddedTestService;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import org.testng.annotations.Guice;

@Guice(modules = IntegrationTestModule.class)
public class IntegrationTestModule extends AbstractModule {

    private final JeroMQEmbeddedTestService embeddedTestService = new JeroMQEmbeddedTestService();

    @Override
    protected void configure() {
        bind(Context.class).toProvider(embeddedTestService::getContext);
        bind(JeroMQEmbeddedTestService.class).toInstance(embeddedTestService.start());
    }

    public JeroMQEmbeddedTestService getEmbeddedTestService() {
        return embeddedTestService;
    }

}
