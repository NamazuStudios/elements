package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.TypeLiteral;
import dev.getelements.elements.dao.mongo.provider.IndexConflictResolver;
import dev.getelements.elements.dao.mongo.provider.MongoIndexConflictResolver;
import dev.getelements.elements.dao.mongo.provider.MorphiaSelfHealingIndexApplier;
import dev.getelements.elements.dao.mongo.provider.SelfHealingAtomicReferenceDataStoreProvider;
import dev.getelements.elements.dao.mongo.provider.SelfHealingIndexApplier;
import dev.getelements.elements.dao.mongo.provider.SelfHealingMorphiaConfigProvider;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;

import java.util.concurrent.atomic.AtomicReference;

public class MongoDaoElementModule extends SharedElementModule {

    public MongoDaoElementModule() {
        super("dev.getelements.elements.sdk.dao");
    }

    @Override
    protected void configureElement() {
        // Bind these directly here (not in nested MongoDaoModule) so that expose() below
        // sees a direct binding — Guice's PrivateModule.expose() does not accept forwarding
        // bindings propagated up from child private modules.
        bind(MorphiaConfig.class)
                .toProvider(SelfHealingMorphiaConfigProvider.class);
        bind(new TypeLiteral<AtomicReference<Datastore>>(){})
                .toProvider(SelfHealingAtomicReferenceDataStoreProvider.class)
                .asEagerSingleton();
        bind(IndexConflictResolver.class).to(MongoIndexConflictResolver.class);
        bind(SelfHealingIndexApplier.class).to(MorphiaSelfHealingIndexApplier.class);
        install(new PreDatastoreMigrationModule());
        install(new MongoDaoModule());
        install(new MongoGridFSLargeObjectBucketModule());
        expose(ElementEntityRegistrar.class);
        // Expose to the outer scope so MongoSdkModule.DatastoreFromRef can inject it.
        expose(new TypeLiteral<AtomicReference<Datastore>>(){});
    }

}
