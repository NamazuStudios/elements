package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.application.SuperUserApplicationService;
import com.namazustudios.socialengine.service.auth.AnonSessionService;
import com.namazustudios.socialengine.service.auth.AnonUsernamePasswordAuthService;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UsernamePasswordAuthService.class).to(AnonUsernamePasswordAuthService.class);
        bind(ApplicationService.class).to(SuperUserApplicationService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();
        bind(SessionService.class).to(AnonSessionService.class);
    }

}
