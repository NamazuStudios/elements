package dev.getelements.elements.appserve;

import com.google.inject.AbstractModule;
import dev.getelements.elements.annotation.FacebookPermission;
import dev.getelements.elements.appserve.guice.*;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.rt.PersistenceStrategy;
import dev.getelements.elements.rt.fst.FSTPayloadReaderWriterModule;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.rt.remote.guice.*;
import dev.getelements.elements.rt.remote.jeromq.guice.*;
import dev.getelements.elements.service.guice.AppleIapReceiptInvokerModule;
import dev.getelements.elements.service.guice.firebase.FirebaseAppFactoryModule;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.rt.PersistenceStrategy.getNullPersistence;

public class AppServeMain implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AppServeMain.class);

    private final Server server;

    public AppServeMain(final String[] args) {
        this(createServer(args));
    }

    @Inject
    public AppServeMain(final Server server) {
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
        final AppServeMain main = new AppServeMain(args);
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
            new AppServeFilterModule(),
            new AppServeSecurityModule(),
            new AppServeServicesModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new ZContextModule(),
            new ClusterContextFactoryModule(),
            new AppServeRedissonServicesmodule(),
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
