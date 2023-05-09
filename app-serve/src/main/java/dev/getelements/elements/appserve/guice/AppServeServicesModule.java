package dev.getelements.elements.appserve.guice;

import dev.getelements.elements.rt.RequestAttributesProvider;
import dev.getelements.elements.rt.guice.RequestScope;
import dev.getelements.elements.service.guice.ServicesModule;

public class AppServeServicesModule extends ServicesModule {

    public AppServeServicesModule() {
        super(RequestScope.getInstance(), RequestAttributesProvider.class);
    }

}
