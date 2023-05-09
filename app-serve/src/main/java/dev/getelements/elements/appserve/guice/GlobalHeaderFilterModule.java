package dev.getelements.elements.appserve.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.servlet.security.HttpServletGlobalSecretHeaderFilter;

public class GlobalHeaderFilterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
    }

}
