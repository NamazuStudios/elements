package com.namazustudios.socialengine.appserve.testkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.appserve.guice.AppServeServicesModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.testkit.TestKit;
import com.namazustudios.socialengine.rt.testkit.UnitTestModule;
import com.namazustudios.socialengine.service.guice.JacksonHttpClientModule;
import com.namazustudios.socialengine.service.guice.OctetStreamJsonMessageBodyReader;
import com.namazustudios.socialengine.util.AppleDateFormat;
import joptsimple.OptionSpec;
import ru.vyarus.guice.validator.ValidationModule;

import java.text.DateFormat;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;

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
                   .addModule(new JacksonHttpClientModule()
                       .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
                       .withDefaultObjectMapperProvider(() -> {
                           final ObjectMapper objectMapper = new ObjectMapper();
                           objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                           return objectMapper;
                       })
                       .withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                           final ObjectMapper objectMapper = new ObjectMapper();
                           final DateFormat dateFormat = new AppleDateFormat();
                           objectMapper.setDateFormat(dateFormat);
                           objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                           objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                           return objectMapper;
                       })
                   );

        testKitMain.run(optionSet -> {

            if (optionSet.valueOf(integration)) {

                testKitMain.addModule(new MongoCoreModule())
                           .addModule(new AppServeServicesModule())
                           .addModule(new MongoDaoModule())
                           .addModule(new MongoSearchModule())
                           .addModule(new ValidationModule())
                           .addModule(new LuaModule());

            } else {

                final UnitTestModule unitTestModule = new UnitTestModule();
                testKitMain.addModule(new LuaModule()
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
