package com.namazustudios.socialengine.codeserve;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.UsernamePasswordAuthService;
import com.namazustudios.socialengine.service.BuildPropertiesVersionService;
import com.namazustudios.socialengine.service.VersionService;
import com.namazustudios.socialengine.service.application.SuperUserApplicationService;
import com.namazustudios.socialengine.service.auth.AnonUsernamePasswordAuthService;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UsernamePasswordAuthService.class).to(AnonUsernamePasswordAuthService.class);
        bind(VersionService.class).to(BuildPropertiesVersionService.class);
        bind(ApplicationService.class).to(SuperUserApplicationService.class);
    }

}
