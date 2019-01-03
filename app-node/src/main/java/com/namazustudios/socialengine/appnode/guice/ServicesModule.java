package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.ServicesDozerMapperProvider;
import com.namazustudios.socialengine.service.advancement.AdvancementService;
import com.namazustudios.socialengine.service.advancement.StandardAdvancementService;
import org.dozer.Mapper;

public class ServicesModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(Mapper.class).toProvider(ServicesDozerMapperProvider.class).asEagerSingleton();
        bind(AdvancementService.class).to(StandardAdvancementService.class);

        expose(AdvancementService.class);
    }
}
