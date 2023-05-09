package dev.getelements.elements.guice;

import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.rt.servlet.HttpServletAttributesProvider;
import dev.getelements.elements.service.guice.ServicesModule;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class StandardServletServicesModule extends ServicesModule {

    public StandardServletServicesModule() {
        super(ServletScopes.REQUEST, HttpServletAttributesProvider.class);
    }

}
