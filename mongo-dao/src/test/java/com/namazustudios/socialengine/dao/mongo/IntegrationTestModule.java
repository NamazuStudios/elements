package com.namazustudios.socialengine.dao.mongo;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.util.ShutdownHooks;
import dev.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

import java.io.*;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class IntegrationTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestModule.class);

    private static final int TEST_MONGO_PORT = 45000;

    private static final String TEST_MONGO_VERSION = "4.1.1";

    private static final String TEST_BIND_IP = "localhost";

    private static final ShutdownHooks hooks = new ShutdownHooks(IntegrationTestModule.class);

    @Override
    protected void configure() {

        try {

            final var uuid = format("%s_%s", getClass().getSimpleName(), randomUUID());

            final var args = new String[] {
                "docker",
                "run",
                "--name",
                uuid,
                "--rm",
                format("-p%d:27017", TEST_MONGO_PORT),
                format("mongo:%s", TEST_MONGO_VERSION)
            };

            final var process = new ProcessBuilder()
                .command(args)
                .start();

            final var stdout = new Thread(log(process::getInputStream, m -> logger.info("mongod {}", m)));
            stdout.setDaemon(true);
            stdout.start();

            final var stderr = new Thread(log(process::getErrorStream, m -> logger.error("mongod {}", m)));
            stderr.setDaemon(true);
            stderr.start();

            hooks.add(process::destroy);
            hooks.add(() -> new ProcessBuilder().command(new String[] {"docker", "kill", uuid}).start());

        } catch (IOException e) {
            addError(e);
            return;
        }

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        install(new ConfigurationModule(() -> {
            final Properties properties = defaultConfigurationSupplier.get();
            properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_BIND_IP, TEST_MONGO_PORT));
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

    public Runnable log(final Supplier<InputStream> inputStreamSupplier,
                        final Consumer<String> messageConsumer) {
        return () -> {
            try (var r = new InputStreamReader(inputStreamSupplier.get());
                 var br = new BufferedReader(r)) {
                final var line = br.readLine();
            } catch (EOFException ex) {
                logger.trace("Hit end of stream.");
            }catch (IOException ex) {
                logger.info("Caught IO Exception reading subprocess.", ex);
            }
        };
    }

}
