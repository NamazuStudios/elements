package dev.getelements.elements.dao.mongo;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import dev.getelements.elements.rt.util.ShutdownHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.util.UUID.randomUUID;

public class MongoTestInstanceModule extends AbstractModule {

    private static final String TEST_MONGO_VERSION = "3.6.23";

    private final int port;

    private final boolean autostart;

    public MongoTestInstanceModule(final int port) {
        this(port, true);
    }

    public MongoTestInstanceModule(final int port, boolean autostart) {
        this.port = port;
        this.autostart = autostart;
    }

    @Override
    protected void configure() {
        if (autostart) {
            bind(MongoTestInstance.class).toProvider(() -> {
                final var instance = new DockerMongoTestInstance(port, TEST_MONGO_VERSION);
                instance.start();
                return instance;
            }).asEagerSingleton();
        } else {
            bind(MongoTestInstance.class).to(DockerMongoTestInstance.class).asEagerSingleton();
        }
    }

}
