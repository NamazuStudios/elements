package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.dao.Indexable;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlan;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.index.IndexPlan;
import dev.morphia.Datastore;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

public class MongoIndexDao implements IndexDao {

    private Mapper mapper;

    private MongoDBUtils mongoDBUtils;

    private Set<Indexable> indexableSet;

    private Provider<Indexer> indexerProvider;

    private Datastore datastore;

    @Override
    public Pagination<IndexPlan<?>> getPlans(final int offset, final int count) {
        final var query = getDatastore().find(MongoIndexPlan.class);
        return getMongoDBUtils().paginationFromQuery(query,offset, count, p -> getMapper().map(p, IndexPlan.class));
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
