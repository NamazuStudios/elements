package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class VersionServletLoader extends GuiceServletContextListener {

    private final Injector containerInjector;

    public VersionServletLoader(final Injector containerInjector) {
        this.containerInjector = containerInjector;
    }

    @Override
    protected Injector getInjector() {
        return containerInjector.createChildInjector(new VersionServletModule());
    }

}
