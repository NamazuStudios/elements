package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.dao.mongo.provider.MongoAtomicReferenceDataStoreProvider;
import dev.getelements.elements.dao.mongo.provider.MorphiaConfigProvider;
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
 * root {@link ElementRegistry} -- so a bare non-Element context (a standalone CLI, a test) can install
 * {@link MongoDaoModule} and {@code MongoSdkModule} directly. The root registry is an empty, unused
 * {@link MutableElementRegistry}: nothing in this context loads Elements, but {@code MongoDaoModule}'s
 * transaction/event-publishing wiring still needs a registry to be bound.
 */
public class MongoDatastoreBootstrapModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(MorphiaConfig.class)
                .toProvider(MorphiaConfigProvider.class);

        bind(new TypeLiteral<AtomicReference<Datastore>>(){})
                .toProvider(MongoAtomicReferenceDataStoreProvider.class)
                .asEagerSingleton();

        bind(ElementRegistry.class)
                .annotatedWith(named(ROOT))
                .to(Key.get(MutableElementRegistry.class, named(ROOT)));

        bind(MutableElementRegistry.class)
                .annotatedWith(named(ROOT))
                .toInstance(MutableElementRegistry.newDefaultInstance());

    }

}
