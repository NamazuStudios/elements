package dev.getelements.elements.sdk.mongo.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.standard.StandardMongoConfigurationService;

public class MongoSdkModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MongoConfigurationService.class)
                .to(StandardMongoConfigurationService.class)
                .asEagerSingleton();
    }

}
