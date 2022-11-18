package com.namazustudios.socialengine.docserve;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.docserve.guice.ServerModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.fst.FSTPayloadReaderWriterModule;
import com.namazustudios.socialengine.rt.jersey.JerseyHttpClientModule;
import com.namazustudios.socialengine.rt.remote.Instance;
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
