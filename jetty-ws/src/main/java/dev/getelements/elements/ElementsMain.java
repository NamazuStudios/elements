package dev.getelements.elements;

import com.google.inject.Guice;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class ElementsMain {

    private static final OptionParser optionParser = new OptionParser();

    private static final OptionSpec<Void> helpOptionSpec = optionParser
            .accepts("help", "Prints Help.")
            .forHelp();

    private static final OptionSpec<ElementsWebService> servicesOptionSpec = optionParser
            .accepts("web-service", "Specifies all web services to run.")
            .withRequiredArg().ofType(ElementsWebService.class)
            .defaultsTo(ElementsWebService.values())
            .ofType(ElementsWebService.class);

    public static void main(final String[] args) throws Exception {
        try {

            final var options = optionParser.parse(args);

            if (options.has(helpOptionSpec)) {
                optionParser.printHelpOn(System.out);
            } else {
                run(options);
            }

        } catch (OptionException ex) {
            System.out.println(ex.getMessage());
            optionParser.printHelpOn(System.out);
            System.exit(1);
        }
    }

    private static void run(final OptionSet options) {

        final var services = servicesOptionSpec.values(options);

        final var injector = Guice.createInjector(
                new ElementsCoreModule(),
                new ElementsWebServiceModule(services)
        );

        final var elements = injector.getInstance(Elements.class);

        try {
            elements.start();
            elements.run();
        } finally {
            elements.stop();
        }

    }

}
