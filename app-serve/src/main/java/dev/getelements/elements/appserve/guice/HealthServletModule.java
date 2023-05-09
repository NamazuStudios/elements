package dev.getelements.elements.appserve.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.servlet.security.HealthServlet;

public class HealthServletModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HealthServlet.class).asEagerSingleton();
    }

}
