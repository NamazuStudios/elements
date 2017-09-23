package com.namazustudios.socialengine.appserve;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appserve.guice.ServerModule;
import com.namazustudios.socialengine.appserve.guice.ServicesModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import org.mortbay.jetty.Server;

public class Main {

    public static void main(final String[] args) throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Injector injector = Guice.createInjector(
            new ServerModule(),
            new ServicesModule(),
            new ConfigurationModule(defaultConfigurationSupplier)
        );

        final Server server = injector.getInstance(Server.class);
        server.start();
        server.join();

    }

}
