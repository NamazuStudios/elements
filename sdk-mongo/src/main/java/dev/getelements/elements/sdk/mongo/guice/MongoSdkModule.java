package dev.getelements.elements.sdk.mongo.guice;

import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.provider.MongoClientProvider;
import dev.getelements.elements.sdk.mongo.provider.MongoDatabaseProvider;
import dev.getelements.elements.sdk.mongo.provider.MongoSslSettingsProvider;
import dev.getelements.elements.sdk.mongo.StandardMongoConfigurationService;

public class MongoSdkModule extends AbstractModule {

    @Override
    protected void configure() {

        binder().bind(MongoClient.class)
                .toProvider(MongoClientProvider.class)
                .asEagerSingleton();

        binder().bind(SslSettings.class)
                .toProvider(MongoSslSettingsProvider.class)
                .asEagerSingleton();

        binder().bind(MongoDatabase.class)
                .toProvider(MongoDatabaseProvider.class);

        bind(MongoConfigurationService.class)
                .to(StandardMongoConfigurationService.class)
                .asEagerSingleton();

    }

}
