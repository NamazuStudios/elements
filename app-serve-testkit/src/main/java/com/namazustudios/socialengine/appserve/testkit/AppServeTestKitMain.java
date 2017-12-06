package com.namazustudios.socialengine.appserve.testkit;

import com.namazustudios.socialengine.appserve.guice.ServicesModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.testkit.TestKitMain;
import joptsimple.OptionSpec;
import org.apache.bval.guice.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AppServeTestKitMain {

    private static final Logger logger = LoggerFactory.getLogger(AppServeTestKitMain.class);

    private final TestKitMain testKitMain;

    private final OptionSpec<Boolean> integration;

    /**
     * Creates an isntance of {@link AppServeTestKitMain} with the supplied command line arguments.
     * @param args
     */
    public AppServeTestKitMain(final String[] args) {

        testKitMain = new TestKitMain(args);

        integration = testKitMain.getOptionParser()
                .accepts("integration", "Enables integration test mode.  Connecting the test kit to the running services.")
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
        testKitMain.run(optionSet -> {

            if (optionSet.valueOf(integration)) {

                final DefaultConfigurationSupplier defaultConfigurationSupplier;
                defaultConfigurationSupplier = new DefaultConfigurationSupplier();

                testKitMain.addModule(new MongoCoreModule())
                           .addModule(new ServicesModule())
                           .addModule(new MongoDaoModule())
                           .addModule(new ValidationModule())
                           .addModule(new MongoSearchModule())
                           .addModule(new ConfigurationModule(defaultConfigurationSupplier));

            }

        });

    }

    public static void main(final String[] args) throws Exception {
        final AppServeTestKitMain appServeTestKitMain = new AppServeTestKitMain(args);
        appServeTestKitMain.run();
    }

}
