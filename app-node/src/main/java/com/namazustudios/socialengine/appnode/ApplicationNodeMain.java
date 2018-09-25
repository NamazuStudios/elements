package com.namazustudios.socialengine.appnode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appnode.guice.JaxRSClientModule;
import com.namazustudios.socialengine.appnode.guice.MultiNodeContainerModule;
import com.namazustudios.socialengine.appnode.guice.VersionModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.MultiNodeContainer;
import com.namazustudios.socialengine.rt.jeromq.CommandPreamble;
import com.namazustudios.socialengine.rt.jeromq.Connection;
import com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost;
import com.namazustudios.socialengine.rt.jeromq.StatusRequest;
import com.namazustudios.socialengine.service.firebase.guice.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.service.notification.guice.GuiceStandardNotificationFactoryModule;
import org.apache.bval.guice.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import java.util.Properties;

import static com.namazustudios.socialengine.appnode.Constants.CONTROL_REQUEST_TIMEOUT;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.CONTROL_BIND_ADDR;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_REQUEST;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static org.zeromq.ZMQ.REQ;

/**
 * Hello world!
 *
 */
public class ApplicationNodeMain {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeMain.class);

    public static void main(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Injector injector = Guice.createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new MongoCoreModule(),
            new MongoDaoModule(),
            new ValidationModule(),
            new MongoSearchModule(),
            new RTFilesystemGitLoaderModule(),
            new MultiNodeContainerModule(),
            new FirebaseAppFactoryModule(),
            new GuiceStandardNotificationFactoryModule(),
            new JaxRSClientModule(),
            new VersionModule()
        );

        // quick and dirty arg check - may want to leverage command processing from Setup module

        for (String arg : args) {
            if(arg.equalsIgnoreCase("--status-check")) {
                final Properties properties = defaultConfigurationSupplier.get();

                String statusCheckAddress = properties.getProperty(CONTROL_BIND_ADDR);

                logger.info(format("Performing status check on %s...", statusCheckAddress));

                boolean result = false;

                try (ZContext context = new ZContext()) {
                    try (final Connection connection = from(context, c -> c.createSocket(REQ))) {
                        connection.socket().connect(properties.getProperty(CONTROL_BIND_ADDR));

                        try {
                            connection.socket().setReceiveTimeOut(Integer.parseInt(properties.getProperty(CONTROL_REQUEST_TIMEOUT)));
                        }
                        catch (NumberFormatException e) {
                            // use default timeout
                        }

                        JeroMQSocketHost.issue(connection.socket(), STATUS_REQUEST,  new StatusRequest().getByteBuffer());

                        final ZMsg resp = ZMsg.recvMsg(connection.socket());

                        if(null != resp) {
                            final CommandPreamble respPreamble = new CommandPreamble();

                            respPreamble.getByteBuffer().put(resp.pop().getData());

                            if(respPreamble.commandType.get() == CommandPreamble.CommandType.STATUS_RESPONSE) {
                                // actual content of the StatusResponse is not important right now

                                result = true;
                            }

                        }

                    }

                }

                logger.info("Status check {}", result ? "OK" : "FAIL");
                logger.info("Shutting down.");

                System.exit(result ? 0 : -1);
            }
        }

        final Object lock = new Object();

        try (final MultiNodeContainer container = injector.getInstance(MultiNodeContainer.class)) {

            logger.info("Starting container.");

            container.start();
            logger.info("Container started.");

            synchronized (lock) {
                while (!interrupted()) {
                    lock.wait();
                }
            }

        } catch (InterruptedException ex) {
            logger.info("Interrupted.  Shutting down.");
        }

        logger.info("Container shut down.  Exiting process.");

    }

}
