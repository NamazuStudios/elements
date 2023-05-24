package dev.getelements.elements;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSpec;

public class ElementsMain {

    private static final OptionParser optionParser = new OptionParser();

    private static final OptionSpec<Void> helpOptionSpec = optionParser
            .accepts("help", "Prints Help.")
            .forHelp();

    private static final OptionSpec<ElementsWebService> levelOptionSpec = optionParser
            .accepts("web-service", "Specifies all web services to run.")
            .withRequiredArg().ofType(ElementsWebService.class)
            .defaultsTo(ElementsWebService.values())
            .ofType(ElementsWebService.class);



    public static void main(final String[] args) throws Exception {
        try {

            final var optionSet = optionParser.parse(args);

            if (optionSet.has(helpOptionSpec)) {
                optionParser.printHelpOn(System.out);
                System.exit(0);
            }

            final var webServiceList = levelOptionSpec.values(optionSet);

        } catch (OptionException ex) {
            System.out.println(ex.getMessage());
            optionParser.printHelpOn(System.out);
        }
    }

}
