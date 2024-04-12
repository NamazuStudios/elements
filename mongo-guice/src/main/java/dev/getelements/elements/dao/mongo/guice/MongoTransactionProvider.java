package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.Injector;
import dev.getelements.elements.dao.Transaction;
import dev.morphia.Datastore;

import javax.inject.Inject;
import javax.inject.Provider;

public class MongoTransactionProvider implements Provider<Transaction> {

    private Provider<Injector> injectorProvider;

    @Override
    public Transaction get() {

        final var injector = getInjectorProvider().get();
        final var datastore = injector.getInstance(Datastore.class);

        final var morphiaSession = datastore.startSession();
        morphiaSession.startTransaction();

        return new Transaction() {

            @Override
            public <DaoT> DaoT getDao(Class<DaoT> daoT) {
                return null;
            }

            @Override
            public boolean isCommitted() {
                return morphiaSession.hasActiveTransaction();
            }

            @Override
            public void commit() {
                morphiaSession.commitTransaction();
            }

            @Override
            public void rollback() {
                morphiaSession.abortTransaction();
            }

            @Override
            public void close() {
                morphiaSession.close();
            }

        };
    }

    public Provider<Injector> getInjectorProvider() {
        return injectorProvider;
    }

    @Inject
    public void setInjectorProvider(Provider<Injector> injectorProvider) {
        this.injectorProvider = injectorProvider;
    }

}
