package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.Injector;
import dev.getelements.elements.dao.Transaction;
import dev.morphia.transactions.MorphiaSession;

import javax.inject.Inject;

public class MorphiaGuiceTransaction implements Transaction {

    private Injector injector;

    private MorphiaSession morphiaSession;

    @Override
    public <DaoT> DaoT getDao(final Class<DaoT> daoT) {
        return getInjector().getInstance(daoT);
    }

    @Override
    public boolean isActive() {
        return getMorphiaSession().hasActiveTransaction();
    }

    @Override
    public void commit() {
        getMorphiaSession().commitTransaction();
    }

    @Override
    public void rollback() {
        getMorphiaSession().abortTransaction();
    }

    @Override
    public void close() {
        getMorphiaSession().close();
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public MorphiaSession getMorphiaSession() {
        return morphiaSession;
    }

    @Inject
    public void setMorphiaSession(MorphiaSession morphiaSession) {
        this.morphiaSession = morphiaSession;
    }

}
