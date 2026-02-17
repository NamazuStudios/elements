package dev.getelements.elements.appnode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import dev.getelements.elements.appnode.guice.*;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoGridFSLargeObjectBucketModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.rt.kryo.guice.KryoPayloadReaderWriterModule;
import dev.getelements.elements.rt.guice.SimpleExecutorsModule;
import dev.getelements.elements.rt.jersey.guice.JerseyHttpClientModule;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.SimpleWatchdogServiceModule;
import dev.getelements.elements.rt.remote.Worker;
import dev.getelements.elements.rt.remote.guice.ClusterContextFactoryModule;
import dev.getelements.elements.rt.remote.guice.InstanceDiscoveryServiceModule;
import dev.getelements.elements.rt.remote.guice.PersistentInstanceIdModule;
import dev.getelements.elements.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
import dev.getelements.elements.rt.remote.jeromq.guice.*;
import dev.getelements.elements.rt.remote.watchdog.WatchdogService;
import dev.getelements.elements.service.guice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.validator.ValidationModule;

public class ApplicationNode {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNode.class);

    private Worker worker;

    private Instance instance;

    private WatchdogService watchdogService;

    private final Injector injector;

    private final Object lock = new Object();

    public ApplicationNode(final DefaultConfigurationSupplier defaultConfigurationSupplier,
                           final StorageDriver storageDriver) {

        final var facebookBuiltinPermissionsSupplier = new FacebookBuiltinPermissionsSupplier();

        final Module storageDriverModule;

        switch (storageDriver) {
            case UNIX_FS:
                storageDriverModule = new UnixFSStorageDriverModule();
                break;
            default:
                throw new IllegalArgumentException("Invalid storage driver: " + storageDriver);
        }

        injector = Guice.createInjector(
            storageDriverModule,
            new SimpleWatchdogServiceModule(),
            new ClusterContextFactoryModule(),
            new ConfigurationModule(defaultConfigurationSupplier),
            new InstanceDiscoveryServiceModule(defaultConfigurationSupplier),
            new KryoPayloadReaderWriterModule(),
            new FacebookBuiltinPermissionsModule(facebookBuiltinPermissionsSupplier),
            new PersistentInstanceIdModule(),
            new ZContextModule(),
            new JeroMQSecurityModule(),
            new MasterNodeModule(),
            new JeroMQRemoteInvokerModule(),
            new JeroMQAsyncConnectionServiceModule(),
            new JeroMQInstanceConnectionServiceModule(),
            new JeroMQControlClientModule(),
            new MongoCoreModule(),
            new MongoDaoModule(),
            new MongoGridFSLargeObjectBucketModule(),
            new SimpleRemoteInvokerRegistryModule(),
            new ValidationModule(),
            new WorkerInstanceModule(),
            new SimpleExecutorsModule().withDefaultSchedulerThreads(),
            new AppNodeServicesModule(),
            new AppleIapReceiptInvokerModule(),
            new JerseyHttpClientModule()
        );

    }

    /**
     * Gets the {@link Worker} for this {@link ApplicationNode}.
     *
     * @return the {@link Worker}
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * Starts the ApplicationNode.
     */
    public void start() {
        synchronized (lock) {

            if (worker != null) throw new IllegalStateException("Already running.");
            lock.notifyAll();

            worker = injector.getInstance(Worker.class);
            instance = injector.getInstance(Instance.class);
            watchdogService = injector.getInstance(WatchdogService.class);

            try {
                logger.info("Starting Instance.");
                instance.start();
                watchdogService.start();
                logger.info("Instance started.");
            } catch (Exception ex) {
                worker = null;
                instance = null;
                watchdogService.stop();
                logger.error("Could not deployAvailableApplications ApplicationNode", ex);
                throw ex;
            }

        }
    }

    /**
     * Stops the ApplicationNode.
     */
    public void stop() {
        synchronized (lock) {

            if (worker == null) throw new IllegalStateException("Not currently running.");
            lock.notifyAll();

            try {
                logger.info("Starting Instance.");
                instance.close();
                logger.info("Instance started.");
            } catch (Exception ex) {
                logger.error("Could not deployAvailableApplications ApplicationNode", ex);
                throw ex;
            } finally {
                worker = null;
                instance = null;
                watchdogService.stop();
                watchdogService = null;
            }

        }
    }

    /**
     * Waits for the ApplicationNode to shut down.
     */
    public void waitForShutdown() throws InterruptedException {
        synchronized (lock) {
            while (worker != null) {
                lock.wait();
            }
        }
    }

    public enum StorageDriver {

        /**
         * Uses the UnixFS Storage system.
         */
        UNIX_FS

    }

}
