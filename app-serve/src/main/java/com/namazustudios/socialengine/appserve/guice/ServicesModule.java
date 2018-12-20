package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.model.Version;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.advancement.AdvancementService;
import com.namazustudios.socialengine.service.advancement.StandardAdvancementService;
import com.namazustudios.socialengine.service.application.SuperUserApplicationService;
import com.namazustudios.socialengine.service.auth.AnonSessionService;
import com.namazustudios.socialengine.service.auth.AnonUsernamePasswordAuthService;
import org.dozer.Mapper;

public class ServicesModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(Mapper.class).toProvider(ServicesDozerMapperProvider.class).asEagerSingleton();

        bind(UsernamePasswordAuthService.class).to(AnonUsernamePasswordAuthService.class);
        bind(ApplicationService.class).to(SuperUserApplicationService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();
        bind(SessionService.class).to(AnonSessionService.class);
        bind(AdvancementService.class).to(StandardAdvancementService.class);

        expose(UsernamePasswordAuthService.class);
        expose(ApplicationService.class);
        expose(VersionService.class);
        expose(SessionService.class);
        expose(AdvancementService.class);

    }

}
