package com.namazustudios.socialengine.rpc;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rpc.guice.RpcApiRedissonServicesModule;
import com.namazustudios.socialengine.rpc.guice.ServerModule;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jersey.JerseyHttpClientModule;
import com.namazustudios.socialengine.rt.remote.guice.*;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.*;
import com.namazustudios.socialengine.service.guice.AppleIapReceiptInvokerModule;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

import static com.google.inject.Guice.createInjector;
import static com.namazustudios.socialengine.rt.PersistenceStrategy.getNullPersistence;

public class RpcApiMain implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RpcApiMain.class);

    private final Server server;

    public RpcApiMain(final String[] args) {
        this(createServer(args));
    }

    @Inject
    public RpcApiMain(final Server server) {
        this.server = server;
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        join();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    @Override
    public void run() {
        try {
            start();
            join();
        } catch (Exception ex) {
            logger.error("Encountered error running server.", ex);
        }
    }

    public static void main(final String[] args) throws Exception {
        final RpcApiMain main = new RpcApiMain(args);
        main.start();
        main.join();
    }

    private static Server createServer(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Supplier<List<FacebookPermission>> facebookPermissionListSupplier;
        facebookPermissionListSupplier =  new FacebookBuiltinPermissionsSupplier();

        return createInjector(
                new ConfigurationModule(defaultConfigurationSupplier),
                new InstanceDiscoveryServiceModule(defaultConfigurationSupplier),
                new JeroMQControlClientModule(),
                new SimpleInstanceModule(),
                new MongoCoreModule(),
                new ServerModule(),
                new MongoDaoModule(),
                new ValidationModule(),
                new MongoSearchModule(),
                new ZContextModule(),
                new ClusterContextFactoryModule(),
                new RpcApiRedissonServicesModule(),
                new JeroMQRemoteInvokerModule(),
                new JeroMQInstanceConnectionServiceModule(),
                new JeroMQAsyncConnectionServiceModule(),
                new SimpleRemoteInvokerRegistryModule(),
                new FSTPayloadReaderWriterModule(),
                new RandomInstanceIdModule(),
                new FirebaseAppFactoryModule(),
                new FacebookBuiltinPermissionsModule(facebookPermissionListSupplier),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(PersistenceStrategy.class).toInstance(getNullPersistence());
                    }
                },
                new AppleIapReceiptInvokerModule(),
                new JerseyHttpClientModule()
        ).getInstance(Server.class);

    }

}
