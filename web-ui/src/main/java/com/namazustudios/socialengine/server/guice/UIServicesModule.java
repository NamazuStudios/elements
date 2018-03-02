package com.namazustudios.socialengine.server.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.BuildPropertiesVersionService;
import com.namazustudios.socialengine.service.VersionService;

public class UIServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();
    }

}
