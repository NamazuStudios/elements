package dev.getelements.elements.docserve;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.getelements.elements.annotation.FacebookPermission;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
import dev.getelements.elements.docserve.guice.ServerModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.rt.fst.FSTPayloadReaderWriterModule;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.rt.remote.Instance;
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

public class DocServeMain implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DocServeMain.class);

    private final Server server;

    private final Instance instance;

    public DocServeMain(final String[] args) {
        this(createInjector(args));
    }

    @Inject
    public DocServeMain(final Injector injector) {
        this.server = injector.getInstance(Server.class);
        this.instance = injector.getInstance(Instance.class);
    }

    public void start() throws Exception {
        instance.start();
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        join();
        instance.close();
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
        final var main = new DocServeMain(args);
        main.start();
        main.join();
    }

    private static Injector createInjector(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Supplier<List<FacebookPermission>> facebookPermissionListSupplier;
        facebookPermissionListSupplier =  new FacebookBuiltinPermissionsSupplier();

        return Guice.createInjector(
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
            new JeroMQRemoteInvokerModule(),
            new JeroMQInstanceConnectionServiceModule(),
            new JeroMQAsyncConnectionServiceModule(),
            new SimpleRemoteInvokerRegistryModule(),
            new FSTPayloadReaderWriterModule(),
            new RandomInstanceIdModule(),
            new FirebaseAppFactoryModule(),
            new FacebookBuiltinPermissionsModule(facebookPermissionListSupplier),
            new AppleIapReceiptInvokerModule(),
            new JerseyHttpClientModule()
        );

    }


}
