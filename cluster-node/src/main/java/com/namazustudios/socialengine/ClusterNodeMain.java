package com.namazustudios.socialengine;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQNodeModule;
import org.apache.bval.guice.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.interrupted;

/**
 * Hello world!
 *
 */
public class ClusterNodeMain {

    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeMain.class);

    public static void main(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Injector injector = Guice.createInjector(
                new MongoCoreModule(),
                new JeroMQNodeModule(),
//                new ServerModule(),
//                new ServicesModule(),
                new MongoDaoModule(),
                new ValidationModule(),
                new MongoSearchModule(),
//                new FileSystemGitLoaderModule(),
                new ConfigurationModule(defaultConfigurationSupplier)
        );

        final Object lock = new Object();

        try (final Node node = injector.getInstance(Node.class)) {

            node.start();

            synchronized (lock) {
                while (!interrupted()) {
                    lock.wait();
                }
            }

        } catch (InterruptedException ex) {
            logger.info("Interrupted.  Shutting down.");
        }

    }

}
