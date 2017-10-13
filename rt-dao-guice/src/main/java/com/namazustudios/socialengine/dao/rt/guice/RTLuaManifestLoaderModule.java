package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;

import java.util.function.Function;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTLuaManifestLoaderModule extends AbstractModule {

    @Override
    protected void configure() {

        install(new LuaModule() {
            @Override
            protected void configureFeatures() {
                exposeLuaState();
                enableBasicConverters();
            }
        });

        bind(new TypeLiteral<Function<Application, ManifestLoader>>(){}).toProvider(LuaManifestLoaderProvider.class);

    }

}
