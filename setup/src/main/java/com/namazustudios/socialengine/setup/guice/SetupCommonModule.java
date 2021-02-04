package com.namazustudios.socialengine.setup.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.setup.Setup;
import ru.vyarus.guice.validator.ValidationModule;

public class SetupCommonModule extends AbstractModule {

    @Override
    protected void configure() {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier(Setup.class.getClassLoader());

        final FacebookBuiltinPermissionsSupplier facebookBuiltinPermissionsSupplier;
        facebookBuiltinPermissionsSupplier = new FacebookBuiltinPermissionsSupplier();

        install(new ConfigurationModule(defaultConfigurationSupplier));
        install(new FacebookBuiltinPermissionsModule(facebookBuiltinPermissionsSupplier));
        install(new MongoCoreModule());
        install(new MongoDaoModule());
        install(new MongoSearchModule());
        install(new ValidationModule());

    }

}
