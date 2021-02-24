package com.namazustudios.socialengine.rt.git;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.ApplicationBootstrapper;
import com.namazustudios.socialengine.rt.id.ApplicationId;

import java.util.Collections;
import java.util.function.Function;

public class BareBootstrapResourcesModule extends PrivateModule {

    @Override
    protected void configure() {

        // Right now there is only one bootstrapper, the Lua bootstrapper.  For the sake of simplicity
        // we just return the instance as requested, but this could be expanded to more languages or
        // frameworks in the future.

        final var tl = new TypeLiteral<Function<ApplicationId, ApplicationBootstrapper.BootstrapResources>>(){};
        bind(tl).toInstance(a -> Collections::emptyMap);
        expose(tl);

    }

}
