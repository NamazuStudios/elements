package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.getelements.elements.dao.Transaction;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.morphia.Datastore;
import dev.morphia.transactions.MorphiaSession;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Properties;

public class MongoTransactionProvider implements Provider<Transaction> {

    private Provider<Injector> injectorProvider;

    @Override
    public Transaction get() {

        final var injector = getInjectorProvider().get();
        final var datastore = injector.getInstance(Datastore.class);
        final var properties = injector.getInstance(Properties.class);

        final var morphiaSession = datastore.startSession();
        morphiaSession.startTransaction();

        final var transactionInjector = Guice.createInjector(
                new ConfigurationModule(() -> properties),
                new MongoDaoModule() {

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

    public Provider<Injector> getInjectorProvider() {
        return injectorProvider;
    }

    @Inject
    public void setInjectorProvider(Provider<Injector> injectorProvider) {
        this.injectorProvider = injectorProvider;
    }

}
