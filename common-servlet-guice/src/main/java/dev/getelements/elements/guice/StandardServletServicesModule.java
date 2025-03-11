package dev.getelements.elements.guice;

import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.getelements.elements.service.guice.ServicesModule;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class StandardServletServicesModule extends SharedElementModule {

    public StandardServletServicesModule() {
        super("dev.getelements.elements.sdk.service");
    }

    @Override
    protected void configureElement() {
        install(new ServicesModule(ServletScopes.REQUEST));
    }

}
