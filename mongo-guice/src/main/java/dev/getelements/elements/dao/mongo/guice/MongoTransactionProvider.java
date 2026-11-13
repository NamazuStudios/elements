package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.*;
import com.mongodb.ReadConcern;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import dev.getelements.elements.dao.mongo.provider.MongoDatastoreProvider;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.guice.GuiceStages;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.transactions.MorphiaSession;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.validation.Validator;

import java.util.Properties;
import java.util.function.Consumer;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

public class MongoTransactionProvider implements Provider<Transaction> {

    private Provider<MapperRegistry> mapperProvider;

    private Provider<Validator> validatorProvider;

    private Provider<Datastore> datastoreProvider;

    private Provider<Properties> propertiesProvider;

    private Provider<MongoDatabase> mongoDatabaseProvider;

    private Provider<ElementRegistry> rootElementRegistryProvider;

    @Override
    public Transaction get() {

        final var datastore = getDatastoreProvider().get();
        final var properties = getPropertiesProvider().get();
        final var morphiaSession = datastore.startSession();

        // Start every transaction with an explicit readConcern SNAPSHOT and writeConcern MAJORITY.
        // With no read concern supplied the driver sends none and mongod defaults the transaction
        // to readConcern local (mx, per the TXN log, provenance implicitDefault). Snapshot + majority
        // makes the guarantees explicit: reads observe committed state plus this session's writes,
        // and the commit waits for majority durability. mongod forces majority on commitTransaction
        // itself, and existing Elements DAO usage matches the snapshot contract.
        // NB: this does NOT by itself resolve the transient stale-snapshot reads observed in
        // NamazuStudios/crossfire#20 (A/B tested against mongod 6.0.9 single-node replica set:
        // failures persisted with these options); that investigation continues with MongoDB upstream.
        morphiaSession.startTransaction(TransactionOptions.builder()
                .readConcern(ReadConcern.SNAPSHOT)
                .writeConcern(WriteConcern.MAJORITY)
                .build());

        final var transactionInjector = Guice.createInjector(
                GuiceStages.get(),
                new ConfigurationModule(() -> properties),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Validator.class).toProvider(getValidatorProvider());
                        bind(MongoDatabase.class).toProvider(getMongoDatabaseProvider());
                    }
                },
                new MongoDaoModule() {

                    @Override
                    protected void bindMapper() {
                        bind(MapperRegistry.class).toProvider(getMapperProvider());
                    }

                    @Override
                    protected void bindDatastore() {
                        bind(Datastore.class).toInstance(morphiaSession);
                        bind(MorphiaSession.class).toInstance(morphiaSession);
                        bind(getMainDatastoreKey()).toInstance(datastore);
                    }

                    @Override
                    protected void bindTransaction() {
                        bind(Transaction.class)
                                .to(MorphiaGuiceTransaction.class)
                                .asEagerSingleton();
                    }

                    @Override
                    protected void bindEventPublisher() {

                        bind(new TypeLiteral<Consumer<Event>>() {})
                                .to(MongoTransactionBufferedEventPublisher.class);

                        bind(MongoTransactionBufferedEventPublisher.class)
                                .asEagerSingleton();

                    }

                    @Override
                    protected void bindElementRegistries() {
                        bind(ElementRegistry.class)
                                .annotatedWith(named(ROOT))
                                .toProvider(getRootElementRegistryProvider());
                    }

                    @Override
                    protected void bindElementEntityRegistrar() {
                        // Not needed in a transaction-scoped injector.
                    }

                }
        );

        return transactionInjector.getInstance(Transaction.class);

    }

    public Provider<Datastore> getDatastoreProvider() {
        return datastoreProvider;
    }

    @Inject
    public void setDatastoreProvider(Provider<Datastore> datastoreProvider) {
        this.datastoreProvider = datastoreProvider;
    }

    public Provider<Properties> getPropertiesProvider() {
        return propertiesProvider;
    }

    @Inject
    public void setPropertiesProvider(Provider<Properties> propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    public Provider<MongoDatabase> getMongoDatabaseProvider() {
        return mongoDatabaseProvider;
    }

    @Inject
    public void setMongoDatabaseProvider(Provider<MongoDatabase> mongoDatabaseProvider) {
        this.mongoDatabaseProvider = mongoDatabaseProvider;
    }

    public Provider<MapperRegistry> getMapperProvider() {
        return mapperProvider;
    }

    @Inject
    public void setMapperProvider(Provider<MapperRegistry> mapperProvider) {
        this.mapperProvider = mapperProvider;
    }

    public Provider<Validator> getValidatorProvider() {
        return validatorProvider;
    }

    @Inject
    public void setValidatorProvider(Provider<Validator> validatorProvider) {
        this.validatorProvider = validatorProvider;
    }

    public Provider<ElementRegistry> getRootElementRegistryProvider() {
        return rootElementRegistryProvider;
    }

    @Inject
    public void setRootElementRegistryProvider(@Named(ROOT) Provider<ElementRegistry> rootElementRegistryProvider) {
        this.rootElementRegistryProvider = rootElementRegistryProvider;
    }

}
