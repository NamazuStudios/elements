package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.PrivateModule;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NativeSupport;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.lua.NativeLibLoader;
import com.namazustudios.socialengine.rt.lua.provider.LuaStateProvider;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaModule extends PrivateModule {

    @Override
    protected void configure() {

        // Sets up the library loader
        NativeSupport.getInstance().setLoader(new NativeLibLoader());

        install(new LuaConverterModule());
        install(new LuaManifestLoaderModule());
        bind(LuaState.class).toProvider(LuaStateProvider.class);

        expose(ManifestLoader.class);


    }

}
