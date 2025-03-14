package dev.getelements.elements.dao.mongo.guice;

import dev.getelements.elements.sdk.guice.SharedElementModule;

public class MongoDaoElementModule extends SharedElementModule {

    public MongoDaoElementModule() {
        super("dev.getelements.elements.sdk.dao");
    }

    @Override
    protected void configureElement() {
        install(new MongoDaoModule());
        install(new MongoGridFSLargeObjectBucketModule());
    }

}
