package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.servlet.security.HealthServlet;

public class HealthServletModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HealthServlet.class).asEagerSingleton();
    }

}
