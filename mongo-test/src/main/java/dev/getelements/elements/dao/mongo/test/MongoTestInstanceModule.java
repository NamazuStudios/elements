package dev.getelements.elements.dao.mongo.test;

import com.google.inject.AbstractModule;

public class MongoTestInstanceModule extends AbstractModule {

    private static final String TEST_MONGO_VERSION = "6.0.9";

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
            bind(MongoTestInstance.class)
                    .toProvider(() -> new DockerMongoTestInstance(port, TEST_MONGO_VERSION))
                    .asEagerSingleton();
        }
    }

}
