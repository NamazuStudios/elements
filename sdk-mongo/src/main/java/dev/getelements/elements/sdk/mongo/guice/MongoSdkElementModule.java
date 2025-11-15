package dev.getelements.elements.sdk.mongo.guice;

import dev.getelements.elements.sdk.spi.guice.SpiAwareSharedElementModule;

public class MongoSdkElementModule extends SpiAwareSharedElementModule {
    public MongoSdkElementModule() {
        super("dev.getelements.elements.sdk.mongo");
    }
}
