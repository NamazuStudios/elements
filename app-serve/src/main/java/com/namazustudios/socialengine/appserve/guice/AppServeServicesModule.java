package com.namazustudios.socialengine.appserve.guice;

import com.namazustudios.socialengine.rt.RequestAttributesProvider;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.application.SuperUserApplicationService;
import com.namazustudios.socialengine.service.guice.ServicesModule;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.appserve.DispatcherAppProvider.DISPATCHER_APPLICATION_SERVICE;

public class AppServeServicesModule extends ServicesModule {

    public AppServeServicesModule() {
        super(RequestScope.getInstance(), RequestAttributesProvider.class);
    }

    @Override
    protected void configure() {

        super.configure();

        bind(ApplicationService.class)
                .annotatedWith(named(DISPATCHER_APPLICATION_SERVICE))
                .to(SuperUserApplicationService.class);

        expose(ApplicationService.class)
                .annotatedWith(named(DISPATCHER_APPLICATION_SERVICE));

    }

}
