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
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.concurrent.atomic.AtomicReference;

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

        // Datastore is created and managed by MongoDaoModule's MongoElementEntityRegistrar
        // via AtomicReference<Datastore> (exposed to the outer scope). We delegate here so that
        // elements declaring @ElementDependency("dev.getelements.elements.sdk.mongo") can inject
        // Datastore and always see the live reference updated by entity registration.
        binder().bind(Datastore.class).toProvider(DatastoreFromRef.class);

    }

    private static class DatastoreFromRef implements Provider<Datastore> {
        @Inject private AtomicReference<Datastore> datastoreRef;

        @Override
        public Datastore get() {
            return datastoreRef.get();
        }
    }

}
