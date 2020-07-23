package com.namazustudios.socialengine.appnode;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appnode.guice.JaxRSClientModule;
import com.namazustudios.socialengine.appnode.guice.MultiNodeContainerModule;
import com.namazustudios.socialengine.appnode.guice.ServicesModule;
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

    private static final OptionParser OPTION_PARSER = new OptionParser();

    private static final OptionSpec<String> STATUS_CHECK_OPTION = OPTION_PARSER
        .accepts("status-check", "Performs a status check against the specified host.")
        .withOptionalArg()
        .ofType(String.class);

    public static void main(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        try {

            final OptionSet optionSet = OPTION_PARSER.parse(args);

            if (optionSet.has(STATUS_CHECK_OPTION)) {
                final String connectAddress = optionSet.valueOf(STATUS_CHECK_OPTION);
                final StatusCheck statusCheck = new StatusCheck(connectAddress);
                statusCheck.run();
            } else {
                final ApplicationNode applicationNode = new ApplicationNode(defaultConfigurationSupplier);
                applicationNode.start();
            }
        }

        } catch (OptionException ex) {
            logger.error("Invalid option.", ex);
            System.exit(1);
        }

        logger.info("Container shut down.  Exiting process.");

    }

}
