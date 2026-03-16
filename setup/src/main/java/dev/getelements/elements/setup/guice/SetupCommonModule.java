package dev.getelements.elements.setup.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
import dev.getelements.elements.dao.mongo.guice.MongoGridFSLargeObjectBucketModule;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.guice.FacebookBuiltinPermissionsModule;
import dev.getelements.elements.sdk.mongo.guice.MongoSdkModule;
import dev.getelements.elements.sdk.service.version.VersionService;
import dev.getelements.elements.service.version.BuildPropertiesVersionService;
import ru.vyarus.guice.validator.ValidationModule;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

public class  SetupCommonModule extends AbstractModule {

    @Override
    protected void configure() {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final FacebookBuiltinPermissionsSupplier facebookBuiltinPermissionsSupplier;
        facebookBuiltinPermissionsSupplier = new FacebookBuiltinPermissionsSupplier();

        install(new ConfigurationModule(defaultConfigurationSupplier));
        install(new FacebookBuiltinPermissionsModule(facebookBuiltinPermissionsSupplier));
        install(new MongoSdkModule());
        install(new MongoDaoModule());
        install(new MongoGridFSLargeObjectBucketModule());
        install(new ValidationModule());

        // Build properties.
        bind(VersionService.class).to(BuildPropertiesVersionService.class).asEagerSingleton();
        bind(VersionService.class).annotatedWith(named(UNSCOPED)).to(BuildPropertiesVersionService.class);

    }

}
