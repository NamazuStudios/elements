package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Hello world!
 *
 */
public class ApplicationNodeMain {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeMain.class);

    public static void main(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        // quick and dirty arg check - may want to leverage command processing from Setup module

        for (String arg : args) {
            if(arg.equalsIgnoreCase("--status-check")) {
                final Properties properties = defaultConfigurationSupplier.get();

            }
        }

        final ApplicationNode applicationNode = new ApplicationNode(defaultConfigurationSupplier);
        applicationNode.start();
    }

}
