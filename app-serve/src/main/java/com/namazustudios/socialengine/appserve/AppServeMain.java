package com.namazustudios.socialengine.appserve;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import org.apache.bval.guice.ValidationModule;
import org.eclipse.jetty.server.Server;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import static com.namazustudios.socialengine.rt.PersistenceStrategy.getNullPersistence;

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
            new ZContextModule(),
            new JeroMQMultiplexerModule(),
            new RTFilesystemGitLoaderModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                bind(PersistenceStrategy.class).toInstance(getNullPersistence());
                bind(Client.class).toProvider(AppServeMain::buildClient).asEagerSingleton();
                }
            }
        );

        final Server server = injector.getInstance(Server.class);
        server.start();
        server.join();

    }

    public static Client buildClient() {
        final Client client = ClientBuilder.newClient().register(ObjectMapperContextResolver.class);
        return client;
    }

    @Provider
    public static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
        private final ObjectMapper mapper;

        public ObjectMapperContextResolver() {
            mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }

}
