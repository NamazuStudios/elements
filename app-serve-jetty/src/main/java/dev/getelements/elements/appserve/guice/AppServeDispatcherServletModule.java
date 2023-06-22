package dev.getelements.elements.appserve.guice;

import dev.getelements.elements.guice.BaseServletModule;
import dev.getelements.elements.rt.servlet.DispatcherServlet;

public class AppServeDispatcherServletModule extends BaseServletModule {

    @Override
    protected void configureServlets() {
        bind(DispatcherServlet.class).asEagerSingleton();
        serve("/*").with(DispatcherServlet.class);
        useStandardSecurityFilters();
    }

}
