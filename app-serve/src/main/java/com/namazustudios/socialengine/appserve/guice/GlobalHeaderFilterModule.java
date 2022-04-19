package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.servlet.security.HttpServletGlobalSecretHeaderFilter;

public class GlobalHeaderFilterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
    }

}
