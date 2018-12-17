package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.advancement.AdvancementService;
import com.namazustudios.socialengine.service.advancement.StandardAdvancementService;

public class ServicesModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(AdvancementService.class).to(StandardAdvancementService.class);
        expose(AdvancementService.class);
    }
}
