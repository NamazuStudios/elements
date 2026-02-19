package dev.getelements.elements.setup;

import com.google.inject.Guice;
import dev.getelements.elements.sdk.SystemVersion;
import dev.getelements.elements.service.version.BuildPropertiesVersionService;
import dev.getelements.elements.setup.commands.Root;
import dev.getelements.elements.setup.guice.SetupCommandModule;
import dev.getelements.elements.setup.guice.SetupCommonModule;

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
    public void run(final String[] args) throws Exception {

        SystemVersion.CURRENT.logVersion();

        final var injector = Guice.createInjector(
            new SetupCommonModule(),
            new SetupCommandModule());

        try (var root = injector.getInstance(Root.class)) {
            root.run(args);
        }

    }

    public static void main( String[] args ) throws Exception {
        final Setup setup = new Setup();
        setup.run(args);
    }

}
