package dev.getelements.elements.sdk.mongo.test;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.guice.RootElementRegistryModule;
import dev.getelements.elements.sdk.mongo.guice.MongoSdkElementModule;

public class MongoSdkTestElementModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new MongoSdkElementModule());
        install(new RootElementRegistryModule());
    }
}
