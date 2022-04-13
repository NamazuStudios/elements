package com.namazustudios.socialengine;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.util.ShutdownHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;

public class MongoTestModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(MongoTestModule.class);

    public static final String TEST_COMPONENT = "com.namazustudios.socialengine.MongoTestModule.test";

    private static final int TEST_MONGO_PORT = 45000;

    private static final int CONNECT_POLLING_RATE = 1000;

    private static final int CONNECT_POLLING_CYCLES = 300;

    private static final String TEST_MONGO_VERSION = "3.4.5";

    private static final String TEST_BIND_IP = "localhost";

    public static final String MONGO_CLIENT_URI = "com.namazustudios.socialengine.mongo.uri";

    private static final ShutdownHooks hooks = new ShutdownHooks(MongoTestModule.class);

    @Override
    protected void configure() {

        try {
            logger.info("Starting test mongo process via Docker.");

            final var uuid = format("%s_%s", getClass().getSimpleName(), randomUUID());

            final var process = new ProcessBuilder()
                    .command(
                            "docker",
                            "run",
                            "--name",
                            uuid,
                            "--rm",
                            format("-p%d:27017", TEST_MONGO_PORT),
                            format("mongo:%s", TEST_MONGO_VERSION)
                    )
                    .redirectErrorStream(true)
                    .start();

            final var stdout = new Thread(log(process::getInputStream, m -> logger.info("mongod {}", m)));
            stdout.setDaemon(true);
            stdout.start();

            final var stderr = new Thread(log(process::getErrorStream, m -> logger.error("mongod {}", m)));
            stderr.setDaemon(true);
            stderr.start();

            hooks.add(() -> {

                logger.info("Destroying mongo process.");
                process.destroy();

                final var kill = new ProcessBuilder()
                        .command("docker", "kill", uuid)
                        .start();

                kill.waitFor();

            });

            waitForConnect();

        } catch (IOException | InterruptedException e) {
            addError(e);
            return;
        }

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        install(new ConfigurationModule(() -> {
            final Properties properties = defaultConfigurationSupplier.get();
            properties.put(MONGO_CLIENT_URI, format("mongodb://%s:%d", TEST_BIND_IP, TEST_MONGO_PORT));
            return properties;
        }));
    }

    private void waitForConnect() throws InterruptedException, UnknownHostException {

        final var addr = InetAddress.getByAddress(new byte[]{127,0,0,1});

        for (int i = 0; i < CONNECT_POLLING_CYCLES; ++i) {
            try (final var socket = new Socket(addr, TEST_MONGO_PORT)) {
                break;
            } catch (IOException e) {
                sleep(CONNECT_POLLING_RATE);
            }
        }

    }

    public Runnable log(final Supplier<InputStream> inputStreamSupplier,
                        final Consumer<String> messageConsumer) {
        return () -> {
            try (var r = new InputStreamReader(inputStreamSupplier.get());
                 var br = new BufferedReader(r)) {

                var line = br.readLine();

                while (line != null) {
                    messageConsumer.accept(line);
                    line = br.readLine();
                }

            } catch (EOFException ex) {
                logger.info("Hit end of stream.");
            } catch (IOException ex) {
                logger.info("Caught IO Exception reading subprocess.", ex);
            }
        };
    }
}
