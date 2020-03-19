package com.namazustudios.socialengine.rest;

import com.google.inject.*;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rest.guice.RestAPIModule;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.Properties;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_DB_URLS;
import static com.namazustudios.socialengine.rest.ClientContext.CONTEXT_APPLICATION;
import static com.namazustudios.socialengine.service.RedissonClientProvider.REDIS_URL;
import static de.flapdoodle.embed.mongo.MongodStarter.getDefaultInstance;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;

public class EmbeddedRestApiIntegrationTestModule extends AbstractModule {

    private static final int TEST_MONGO_PORT = 45000;

    private static final String TEST_MONGO_BIND_IP = "127.0.0.1";

    private static final int TEST_REDIS_PORT = 45001;

    private static final String TEST_REDIS_BIND_IP = "127.0.0.1";

    @Override
    protected void configure() {

        try {
            final MongodExecutable executable = mongodExecutable();
            bind(MongodExecutable.class).toInstance(executable);
            bind(MongodProcess.class).toInstance(executable.start());
        } catch (IOException e) {
            addError(e);
            return;
        }

        final RedisServer redisServer;

        try {
            redisServer = new RedisServer(TEST_REDIS_PORT);
            redisServer.start();
            bind(RedisServer.class).toInstance(redisServer);
        } catch (IOException e) {
            addError(e);
            return;
        }

        final Provider<ApplicationDao> applicationDaoProvider = getProvider(ApplicationDao.class);

        bind(Application.class).annotatedWith(named(CONTEXT_APPLICATION)).toProvider(() -> {
            final Application application = new Application();
            application.setName("CXTTAPP");
            application.setDescription("Context Test Application");
            return applicationDaoProvider.get().createOrUpdateInactiveApplication(application);
        }).in(SINGLETON);

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        install(new RestAPIModule(() -> {
            final Properties properties = defaultConfigurationSupplier.get();
            properties.put(REDIS_URL, format("redis://%s:%d", TEST_REDIS_BIND_IP, TEST_REDIS_PORT));
            properties.put(MONGO_DB_URLS, format("mongo://%s:%d", TEST_MONGO_BIND_IP, TEST_MONGO_PORT));
            return properties;
        }));

        bind(RestAPIMain.class).asEagerSingleton();
        bind(EmbeddedRestApi.class).asEagerSingleton();

    }

    public MongodExecutable mongodExecutable() throws IOException {

        final IMongodConfig config = new MongodConfigBuilder()
                .version(Version.V3_4_5)
                .net(new Net(TEST_MONGO_BIND_IP, TEST_MONGO_PORT, localhostIsIPv6()))
                .build();

        final MongodStarter starter = getDefaultInstance();
        return starter.prepare(config);

    }

}
