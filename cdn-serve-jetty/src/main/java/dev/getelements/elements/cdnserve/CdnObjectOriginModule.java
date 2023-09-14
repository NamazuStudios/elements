package dev.getelements.elements.cdnserve;

import dev.getelements.elements.guice.BaseServletModule;

public class CdnObjectOriginModule extends BaseServletModule {

    @Override
    protected void configureServlets() {
        bind(LargeObjectServlet.class).asEagerSingleton();
        serve("/*").with(LargeObjectServlet.class);
        useStandardSecurityFilters();
    }

}
