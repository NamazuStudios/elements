package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.AuthService;
import com.namazustudios.socialengine.service.application.SuperUserApplicationService;
import com.namazustudios.socialengine.service.auth.AnonAuthService;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AuthService.class).to(AnonAuthService.class);
        bind(ApplicationService.class).to(SuperUserApplicationService.class);
    }

}
