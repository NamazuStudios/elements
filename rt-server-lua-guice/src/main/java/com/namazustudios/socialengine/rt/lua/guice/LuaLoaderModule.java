package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.lua.LuaManifestLoader;
import com.namazustudios.socialengine.rt.lua.LuaResourceLoader;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ResourceLoader.class).to(LuaResourceLoader.class);
        bind(ManifestLoader.class).to(LuaManifestLoader.class);
    }

}
