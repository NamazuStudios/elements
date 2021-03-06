package com.namazustudios.socialengine.setup;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.namazustudios.socialengine.setup.commands.Root;
import com.namazustudios.socialengine.setup.guice.ConsoleSecureReaderModule;
import com.namazustudios.socialengine.setup.guice.SetupCommandModule;
import com.namazustudios.socialengine.setup.guice.SetupCommonModule;

import java.util.Properties;

import static org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

/**
 * A Setup utility that can be run from the command-line.  This operates git-style where
 * there are several sub-commands to do things to get the application up and running.
 *
 * This class starts sets up the IoC container, parses out the first command passed, and
 * instantiates the individual {@link SetupCommand} which processes the rest from there.
 *
 */
public class Setup {

    /**
     * Runs the command.
     *
     * @param args the argument list.
     *
     * @throws Exception in case something goes wrong.
     */
    public void run(final String args[]) throws Exception {

        final Properties systemProperties = System.getProperties();

        if (!systemProperties.containsKey(DEFAULT_LOG_LEVEL_KEY)) {
            systemProperties.setProperty(DEFAULT_LOG_LEVEL_KEY, "warn");
        }

        final var injector = Guice.createInjector(
            new SetupCommonModule(),
            new SetupCommandModule(),
            new ConsoleSecureReaderModule());

        try (var root = injector.getInstance(Root.class)) {
            root.run(args);
        }

    }

    public static void main( String[] args ) throws Exception {
        final Setup setup = new Setup();
        setup.run(args);
    }

}
