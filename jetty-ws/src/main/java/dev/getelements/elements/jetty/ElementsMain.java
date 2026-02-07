package dev.getelements.elements.jetty;

import com.google.inject.Guice;
import dev.getelements.elements.deployment.jetty.guice.JettySdkElementModule;
import dev.getelements.elements.rt.git.FileSystemElementStorageGitLoaderModule;
import dev.getelements.elements.service.version.BuildPropertiesVersionService;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class ElementsMain {

    private static final OptionParser optionParser = new OptionParser();

    private static final OptionSpec<Void> helpOptionSpec = optionParser
            .accepts("help", "Prints Help.")
            .forHelp();

    private static final OptionSpec<ElementsWebServiceComponent> servicesOptionSpec = optionParser
            .accepts("web-service", "Specifies all web services to run.")
            .withRequiredArg()
            .ofType(ElementsWebServiceComponent.class)
            .defaultsTo(ElementsWebServiceComponent.values())
            .ofType(ElementsWebServiceComponent.class);

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

        BuildPropertiesVersionService.logVersion();

        final var services = servicesOptionSpec.values(options);

        final var injector = Guice.createInjector(
                new JettyServerModule(),
                new ElementsCoreModule(),
                new JettySdkElementModule(),
                new FileSystemElementStorageGitLoaderModule(),
                new ElementsWebServiceComponentModule(services)
        );

        final var elements = injector.getInstance(ElementsWebServices.class);

        try {
            elements.start();
            elements.run();
        } finally {
            elements.stop();
        }

    }

}
