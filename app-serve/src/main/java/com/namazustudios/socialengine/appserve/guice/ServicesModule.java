package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.application.SuperUserApplicationService;
import com.namazustudios.socialengine.service.auth.AnonAuthService;
import com.namazustudios.socialengine.service.auth.StandardFacebookAuthService;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AuthService.class).to(AnonAuthService.class);
        bind(ApplicationService.class).to(SuperUserApplicationService.class);
        bind(FacebookAuthService.class).to(StandardFacebookAuthService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class);
    }

}
