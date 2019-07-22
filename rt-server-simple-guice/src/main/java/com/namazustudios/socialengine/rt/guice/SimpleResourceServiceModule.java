package com.namazustudios.socialengine.rt.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;

import java.util.Deque;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.SimpleSingleUseHandlerService.RESOURCE_SERVICE;

public class SimpleResourceServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(ResourceService.class)
            .annotatedWith(named(RESOURCE_SERVICE))
            .to(SimpleResourceService.class)
            .asEagerSingleton();

        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>() {})
            .toProvider(() -> new ProxyLockService<>(Deque.class));

        bind(new TypeLiteral<OptimisticLockService<ResourceId>>() {})
            .to(SimpleResourceIdOptimisticLockService.class);

        final Key<ResourceService> key = Key.get(ResourceService.class, named(RESOURCE_SERVICE));
        expose(key);

    }

}
