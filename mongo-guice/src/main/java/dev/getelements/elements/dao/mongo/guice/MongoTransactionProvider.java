package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import com.mongodb.client.MongoDatabase;
import dev.getelements.elements.dao.Transaction;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.morphia.Datastore;
import dev.morphia.transactions.MorphiaSession;
import org.dozer.Mapper;
import ru.vyarus.guice.validator.ValidationModule;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Validator;
import java.util.Properties;

public class MongoTransactionProvider implements Provider<Transaction> {

    private Provider<Mapper> mapperProvider;

    private Provider<Validator> validatorProvider;

    private Provider<Datastore> datastoreProvider;

    private Provider<Properties> propertiesProvider;

    private Provider<MongoDatabase> mongoDatabaseProvider;

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
                        bind(Mapper.class).toProvider(getMapperProvider());
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

    public Provider<Mapper> getMapperProvider() {
        return mapperProvider;
    }

    @Inject
    public void setMapperProvider(Provider<Mapper> mapperProvider) {
        this.mapperProvider = mapperProvider;
    }

    public Provider<Validator> getValidatorProvider() {
        return validatorProvider;
    }

    @Inject
    public void setValidatorProvider(Provider<Validator> validatorProvider) {
        this.validatorProvider = validatorProvider;
    }

}
