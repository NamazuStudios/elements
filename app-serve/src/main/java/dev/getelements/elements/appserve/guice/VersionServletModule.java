package dev.getelements.elements.appserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.servlet.security.VersionServlet;

public class VersionServletModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(VersionServlet.class).asEagerSingleton();
    }

}
