package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.ManifestLoader;

import java.util.function.Function;

public class RTGitApplicationModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(new TypeLiteral<Function<Application, Injector>>(){})
            .toProvider(RTGitApplicationInjectorProvider.class);

        bind(new TypeLiteral<Function<Application, ManifestLoader>>(){})
            .toProvider(new RTApplicationInjectorScopedProvider<>(ManifestLoader.class));

        expose(new TypeLiteral<Function<Application, ManifestLoader>>(){});

    }

}
