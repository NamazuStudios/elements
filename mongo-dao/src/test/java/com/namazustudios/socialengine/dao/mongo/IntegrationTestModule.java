package com.namazustudios.socialengine.dao.mongo;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.util.ShutdownHooks;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import dev.morphia.Datastore;
import ru.vyarus.guice.validator.ValidationModule;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static de.flapdoodle.embed.mongo.MongodStarter.getDefaultInstance;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static java.lang.String.format;

public class IntegrationTestModule extends AbstractModule {

    private static final AtomicInteger TEST_MONGO_PORT = new AtomicInteger(45000);

    private static final String TEST_BIND_IP = "localhost";

    private static final ShutdownHooks hooks = new ShutdownHooks(IntegrationTestModule.class);

    @Override
    protected void configure() {

        final int port = TEST_MONGO_PORT.getAndIncrement();

        try {
            final var executable = mongodExecutable(port);
            bind(MongodExecutable.class).toInstance(executable);
            bind(MongodProcess.class).toInstance(executable.start());
            hooks.add(this, executable::stop);
        } catch (IOException e) {
            addError(e);
            return;
        }

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        install(new ConfigurationModule(() -> {
            final Properties properties = defaultConfigurationSupplier.get();
            properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_BIND_IP, port));
            return properties;
        }));

        install(new MongoDaoModule(){
            @Override
            protected void configure() {
                super.configure();
                expose(Datastore.class);
            }
        });

        bind(UserTestFactory.class).asEagerSingleton();

        install(new MongoCoreModule());
        install(new MongoSearchModule());
        install(new ValidationModule());

    }

    public MongodExecutable mongodExecutable(final int port) throws IOException {

        final MongodConfig config = MongodConfig.builder()
            .version(Version.V3_4_5)
            .net(new Net(TEST_BIND_IP, port, localhostIsIPv6()))
            .build();

        final MongodStarter starter = getDefaultInstance();
        return starter.prepare(config);

    }

}
