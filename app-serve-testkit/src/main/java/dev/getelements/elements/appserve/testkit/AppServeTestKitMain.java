package dev.getelements.elements.appserve.testkit;

import dev.getelements.elements.appserve.guice.AppServeServicesModule;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoGridFSLargeObjectBucketModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.rt.guice.GuiceIoCResolverModule;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.rt.lua.guice.LuaModule;
import dev.getelements.elements.rt.testkit.TestKit;
import dev.getelements.elements.rt.testkit.UnitTestModule;
import joptsimple.OptionSpec;
import ru.vyarus.guice.validator.ValidationModule;

public class AppServeTestKitMain {

    private final TestKit testKitMain;

    private final OptionSpec<Boolean> integration;

    /**
     * Creates an isntance of {@link AppServeTestKitMain} with the supplied command line arguments.
     * @param args
     */
    public AppServeTestKitMain(final String[] args) {

        testKitMain = new TestKit(args);

        integration = testKitMain.getOptionParser()
            .accepts("integration", "Enables integration test mode.  Connecting the test kit " +
                                                      "to the running services (MongoDB, Redis etc.)")
            .withRequiredArg()
            .ofType(Boolean.class)
            .defaultsTo(false);

    }

    /**
     * Runs all tests.
     *
     * @throws Exception
     */
    public void run() throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        testKitMain.addModule(new GuiceIoCResolverModule())
                   .addModule(new ConfigurationModule(defaultConfigurationSupplier))
                   .addModule(new JerseyHttpClientModule());

        testKitMain.run(optionSet -> {

            if (optionSet.valueOf(integration)) {

                testKitMain.addModule(new MongoCoreModule())
                           .addModule(new MongoGridFSLargeObjectBucketModule())
                           .addModule(new AppServeServicesModule())
                           .addModule(new MongoDaoModule())
                           .addModule(new ValidationModule())
                           .addModule(new LuaModule());

            } else {

                final UnitTestModule unitTestModule = new UnitTestModule();
                testKitMain
                    .addModule(new LuaModule()
                        .visitDiscoveredModule((e, c) -> unitTestModule.mock(c))
                        .visitDiscoveredExtension((s, c) -> unitTestModule.mock(c)))
                    .addModule(unitTestModule);

            }

        });

    }

    public static void main(final String[] args) throws Exception {
        final AppServeTestKitMain appServeTestKitMain = new AppServeTestKitMain(args);
        appServeTestKitMain.run();
    }

}
