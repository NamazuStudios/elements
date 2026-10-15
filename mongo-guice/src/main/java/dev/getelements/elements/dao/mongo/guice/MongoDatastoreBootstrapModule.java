package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.dao.mongo.provider.*;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;

import java.util.concurrent.atomic.AtomicReference;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

/**
 * Binds the handful of things {@link MongoDaoModule} otherwise expects an Element-loading server (or
 * {@link MongoDaoElementModule}) to provide -- {@link MorphiaConfig}, {@code AtomicReference<Datastore>}, and a
 * root {@link ElementRegistry} -- so a bare non-Element context (the {@code migrate} CLI, a test) can install
 * {@link MongoDaoModule} and {@code MongoSdkModule} directly. The root registry is an empty, unused
 * {@link MutableElementRegistry}: nothing in this context loads Elements, but {@code MongoDaoModule}'s
 * transaction/event-publishing wiring still needs a registry to be bound.
 *
 * <p>This is also where index self-healing lives: the migrate tool's datastore routes through
 * {@link SelfHealingAtomicReferenceDataStoreProvider} so that running {@code migrate} resolves stale on-disk
 * index conflicts (dropping the divergent index and recreating the declared spec) as an explicit ops step.
 * The server boot path binds the plain {@link MorphiaConfigProvider}/{@link MongoAtomicReferenceDataStoreProvider}
 * pair and does not heal -- see {@link SelfHealingAtomicReferenceDataStoreProvider}.</p>
 */
public class MongoDatastoreBootstrapModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(MorphiaConfig.class)
                .toProvider(SelfHealingMorphiaConfigProvider.class);

        bind(new TypeLiteral<AtomicReference<Datastore>>(){})
                .toProvider(SelfHealingAtomicReferenceDataStoreProvider.class)
                .asEagerSingleton();

        install(new PreDatastoreMigrationModule());

        bind(IndexConflictResolver.class).to(MongoIndexConflictResolver.class);

        bind(SelfHealingIndexApplier.class).to(MorphiaSelfHealingIndexApplier.class);

        bind(ElementRegistry.class)
                .annotatedWith(named(ROOT))
                .to(Key.get(MutableElementRegistry.class, named(ROOT)));

        bind(MutableElementRegistry.class)
                .annotatedWith(named(ROOT))
                .toInstance(MutableElementRegistry.newDefaultInstance());

    }

}
