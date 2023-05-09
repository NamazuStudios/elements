package dev.getelements.elements.codeserve;

import com.google.inject.AbstractModule;
import dev.getelements.elements.service.ApplicationService;
import dev.getelements.elements.service.UsernamePasswordAuthService;
import dev.getelements.elements.service.BuildPropertiesVersionService;
import dev.getelements.elements.service.VersionService;
import dev.getelements.elements.service.application.SuperUserApplicationService;
import dev.getelements.elements.service.auth.AnonUsernamePasswordAuthService;

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
