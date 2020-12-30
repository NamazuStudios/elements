package com.namazustudios.socialengine.codeserve;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.ApplicationBootstrapper;
import com.namazustudios.socialengine.rt.ApplicationBootstrapper.BootstrapResources;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.lua.LuaBootstrapResources;

import java.util.function.Function;

public class BootstrapResourcesModule extends PrivateModule {

    @Override
    protected void configure() {

        // Right now there is only one bootstrapper, the Lua bootstrapper.  For the sake of simplicity
        // we just return the instance as requested, but this could be expanded to more languages or
        // frameworks in the future.

        final var tl = new TypeLiteral<Function<ApplicationId, BootstrapResources>>(){};
        bind(tl).toInstance(a -> new LuaBootstrapResources());
        expose(tl);

    }

}
