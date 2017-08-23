package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.rt.provider.LuaManifestLoaderProvider;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.ManifestLoader;

import java.util.function.Function;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTLuaManifestLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<Function<Application, ManifestLoader>>(){}).toProvider(LuaManifestLoaderProvider.class);
    }

}
