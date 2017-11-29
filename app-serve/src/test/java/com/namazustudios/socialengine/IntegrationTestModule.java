package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ClasspathAssetLoader;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import org.testng.annotations.Guice;

@Guice(modules = IntegrationTestModule.class)
public class IntegrationTestModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new LuaModule());
        install(new SimpleContextModule());
        bind(AssetLoader.class).toProvider(() -> new ClasspathAssetLoader(getClass().getClassLoader()));
    }

}
