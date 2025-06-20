package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import com.mongodb.client.MongoDatabase;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.transactions.MorphiaSession;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.validation.Validator;

import java.util.Properties;

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

        morphiaSession.startTransaction();

        final var transactionInjector = Guice.createInjector(
                Stage.PRODUCTION,
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
                    }

                    @Override
                    protected void bindTransaction() {
                        bind(Transaction.class)
                                .to(MorphiaGuiceTransaction.class)
                                .asEagerSingleton();
                    }

                    @Override
                    protected void bindElementRegistries() {
                        bind(ElementRegistry.class)
                                .annotatedWith(named(ROOT))
                                .toProvider(getRootElementRegistryProvider());
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
