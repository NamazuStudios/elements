package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.lua.LuaManifestLoader;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaManifestLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ManifestLoader.class).to(LuaManifestLoader.class);
    }

}
