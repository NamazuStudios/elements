package com.namazustudios.socialengine.appserve;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.inject.AbstractModule;
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
import com.namazustudios.socialengine.rt.NullResourceAcquisition;
import com.namazustudios.socialengine.rt.ResourceAcquisition;
import org.apache.bval.guice.ValidationModule;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class AppServeMain {

    public static void main(final String[] args) throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Injector injector = Guice.createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new MongoCoreModule(),
            new ServerModule(),
            new ServicesModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new JeroMQMultiplexerModule(),
            new RTFilesystemGitLoaderModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(ResourceAcquisition.class).to(NullResourceAcquisition.class);
                    bind(Client.class).toProvider(AppServeMain::buildClient).asEagerSingleton();
                }
            }
        );

        final Server server = injector.getInstance(Server.class);
        server.start();
        server.join();

    }

    public static Client buildClient() {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Client client = ClientBuilder.newClient(new ClientConfig(jacksonJsonProvider));
        return client;
    }

}
