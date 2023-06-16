package dev.getelements.elements.docserve.guice;

import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.rt.servlet.HttpServletAttributesProvider;
import dev.getelements.elements.service.guice.ServicesModule;

public class DocServeServicesModule extends ServicesModule {

    public DocServeServicesModule() {
        super(ServletScopes.REQUEST, HttpServletAttributesProvider.class);
    }

}
