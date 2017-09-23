package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.rt.servlet.DispatcherServlet;

public class DispatcherServletMappings extends ServletModule {

    @Override
    protected void configureServlets() {
        serve("/*").with(DispatcherServlet.class);
    }

}
