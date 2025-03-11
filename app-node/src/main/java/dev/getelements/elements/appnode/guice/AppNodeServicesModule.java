package dev.getelements.elements.appnode.guice;

import dev.getelements.elements.rt.guice.ResourceScope;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.getelements.elements.service.guice.ServicesModule;

public class AppNodeServicesModule extends SharedElementModule {

    public AppNodeServicesModule() {
        super("dev.getelements.elements.service");
    }

    @Override
    protected void configureElement() {
        ResourceScope.bind(binder());
        install(new ServicesModule(ResourceScope.getInstance()));
    }

}
