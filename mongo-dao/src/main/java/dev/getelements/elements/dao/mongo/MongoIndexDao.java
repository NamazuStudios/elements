package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.dao.Indexable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

public class MongoIndexDao implements IndexDao {

    private Set<Indexable> indexableSet;

    private Provider<Indexer> indexerProvider;

    @Override
    public void plan() {
        getIndexableSet().forEach(Indexable::plan);
    }

    @Override
    public Indexer beginIndexing() {
        return getIndexerProvider().get();
    }

    public Set<Indexable> getIndexableSet() {
        return indexableSet;
    }

    @Inject
    public void setIndexableSet(Set<Indexable> indexableSet) {
        this.indexableSet = indexableSet;
    }

    public Provider<Indexer> getIndexerProvider() {
        return indexerProvider;
    }

    @Inject
    public void setIndexerProvider(Provider<Indexer> indexerProvider) {
        this.indexerProvider = indexerProvider;
    }

}
