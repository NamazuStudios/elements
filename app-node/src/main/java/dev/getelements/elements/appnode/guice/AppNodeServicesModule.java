package dev.getelements.elements.appnode.guice;

import dev.getelements.elements.rt.ResourceAttributesProvider;
import dev.getelements.elements.rt.guice.ResourceScope;
import dev.getelements.elements.service.guice.ServicesModule;

public class AppNodeServicesModule extends ServicesModule {

    public AppNodeServicesModule() {
        super(ResourceScope.getInstance(), ResourceAttributesProvider.class);
    }

    @Override
    protected void configure() {
        super.configure();
        ResourceScope.bind(binder());
    }

}
