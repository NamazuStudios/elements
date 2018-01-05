package com.namazustudios.socialengine.appserve;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appserve.guice.JeroMQMultiplexerModule;
import com.namazustudios.socialengine.appserve.guice.ServerModule;
import com.namazustudios.socialengine.appserve.guice.ServicesModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import org.apache.bval.guice.ValidationModule;
import org.eclipse.jetty.server.Server;

public class AppServeMain {

    public static void main(final String[] args) throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Injector injector = Guice.createInjector(
            new MongoCoreModule(),
            new ServerModule(),
            new ServicesModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new JeroMQMultiplexerModule(),
            new RTFilesystemGitLoaderModule(),
            new ConfigurationModule(defaultConfigurationSupplier)
        );

        final Server server = injector.getInstance(Server.class);
        server.start();
        server.join();

    }

}
