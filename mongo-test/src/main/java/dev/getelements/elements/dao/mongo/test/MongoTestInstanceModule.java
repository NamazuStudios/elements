package dev.getelements.elements.dao.mongo.test;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.spi.ProvisionListener;
import dev.getelements.elements.sdk.mongo.test.DockerMongoTestInstance;
import dev.getelements.elements.sdk.mongo.test.MongoTestInstance;

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

        bind(MongoTestInstance.class)
                .toProvider(() -> new DockerMongoTestInstance(port, TEST_MONGO_VERSION))
                .asEagerSingleton();

        if (autostart) {
            bindListener(
                    this::isMongoTestInstance,
                    new ProvisionListener() {
                @Override
                public <T> void onProvision(final ProvisionInvocation<T> provision) {
                    final MongoTestInstance instance = (MongoTestInstance) provision.provision();
                    instance.start();
                }
            });
        }
    }

    private boolean isMongoTestInstance(final Binding<?> binding) {
        return MongoTestInstance.class.isAssignableFrom(binding.getKey().getTypeLiteral().getRawType());
    }

}
