package dev.getelements.elements.appnode;

import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.service.version.BuildPropertiesVersionService;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        .ofType(String.class)
        .defaultsTo("tcp://localhost:28883");

    private static final OptionSpec<ApplicationNode.StorageDriver> STORAGE_DRIVER_OPTION = OPTION_PARSER
        .accepts("storage-driver", "Runs with the UnixFS Storage Driver")
        .withRequiredArg()
        .ofType(ApplicationNode.StorageDriver.class)
        .defaultsTo(ApplicationNode.StorageDriver.UNIX_FS);

    public static void main(final String[] args) throws InterruptedException {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        try {

            final OptionSet optionSet = OPTION_PARSER.parse(args);

            if (optionSet.has(STATUS_CHECK_OPTION)) {
                final var connectAddress = optionSet.valueOf(STATUS_CHECK_OPTION);
                final var statusCheck = new StatusCheck(connectAddress);
                statusCheck.run();
            } else {
                final var storageDriver = optionSet.valueOf(STORAGE_DRIVER_OPTION);
                final var applicationNode = new ApplicationNode(defaultConfigurationSupplier, storageDriver);
                applicationNode.start();
                applicationNode.waitForShutdown();
            }

        } catch (OptionException ex) {
            logger.error("Invalid option.", ex);
            System.exit(1);
        }

        logger.info("Container shut down.  Exiting process.");

    }

}
