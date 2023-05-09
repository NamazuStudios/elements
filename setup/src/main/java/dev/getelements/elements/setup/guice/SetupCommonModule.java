package dev.getelements.elements.setup.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.service.BuildPropertiesVersionService;
import dev.getelements.elements.service.Unscoped;
import dev.getelements.elements.service.VersionService;
import dev.getelements.elements.setup.Setup;
import ru.vyarus.guice.validator.ValidationModule;

public class  SetupCommonModule extends AbstractModule {

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

        // Build properties.
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();
        bind(VersionService.class).annotatedWith(Unscoped.class).to(BuildPropertiesVersionService.class);

    }

}
