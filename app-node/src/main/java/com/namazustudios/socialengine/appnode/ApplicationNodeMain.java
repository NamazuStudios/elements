package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class ApplicationNodeMain {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeMain.class);

    private static final OptionParser OPTION_PARSER = new OptionParser();

    private static final OptionSpec<String> STATUS_CHECK_OPTION = OPTION_PARSER
        .accepts("--status-check", "Performs a status check against the specified host.")
        .withOptionalArg()
        .ofType(String.class);

    private static final OptionSpec<Boolean> RUN_OPTION = OPTION_PARSER
        .accepts("--run", "Performs a status check against the specified host.")
        .withOptionalArg()
        .ofType(Boolean.class);

    public static void main(final String[] args) throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final OptionSet optionSet = OPTION_PARSER.parse(args);

        try {
            if (optionSet.has(STATUS_CHECK_OPTION)) {
                final String connectAddress = optionSet.valueOf(STATUS_CHECK_OPTION);
                final StatusCheck statusCheck = new StatusCheck(connectAddress);
                statusCheck.run();
            } else if (optionSet.has(RUN_OPTION)) {
                final ApplicationNode applicationNode = new ApplicationNode(defaultConfigurationSupplier);
                applicationNode.start();
            } else {
                logger.error("One of --status-check or --run must be specified.");
                OPTION_PARSER.printHelpOn(System.out);
                System.exit(1);
            }
        } catch (OptionException ex) {
            logger.error("Invalid option.", ex);
            System.exit(1);
        }

    }

}
