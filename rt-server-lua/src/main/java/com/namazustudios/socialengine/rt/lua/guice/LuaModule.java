package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.naef.jnlua.NativeSupport;
import com.namazustudios.socialengine.rt.lua.NativeLibLoader;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaModule extends AbstractModule {

    @Override
    protected void configure() {
        NativeSupport.getInstance().setLoader(new NativeLibLoader());
        install(new LuaDozerModule());
        install(new LuaManifestLoaderModule());
    }

}
