package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.namazustudios.elements.fts.mongo.Condition;
import com.namazustudios.elements.fts.mongo.MongoLockFactory;
import com.namazustudios.elements.fts.mongo.MongoLockFactoryBuilder;
import com.namazustudios.socialengine.util.ShutdownHooks;
import org.apache.lucene.store.LockFactory;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class MongoLockFactoryProvider implements Provider<LockFactory> {

    public static final String LOCK_COLLECTION = "com.namazustudios.socialengine.mongo.search.index.lock.collection";

    public static final String PEER_COLLECTION = "com.namazustudios.socialengine.mongo.search.index.peer.collection";

    private Provider<Condition> conditionProvider;

    private String lockCollectionName;

    private String peerCollectionName;

    private MongoDatabase mongoDatabase;

    private final ShutdownHooks shutdownHooks = new ShutdownHooks(MongoLockFactoryProvider.class);

    @Override
    public LockFactory get() {

        final Condition condition = getConditionProvider().get();
        final MongoCollection<Document> lockCollection = mongoDatabase.getCollection(getLockCollectionName());
        final MongoCollection<Document> peerCollection = mongoDatabase.getCollection(getPeerCollectionName());

        final MongoLockFactory mongoLockFactory = new MongoLockFactoryBuilder()
            .withCondition(condition)
            .build(lockCollection, peerCollection);

        shutdownHooks.add(mongoLockFactory, () -> mongoLockFactory.close());
        return mongoLockFactory;

    }

    public Provider<Condition> getConditionProvider() {
        return conditionProvider;
    }

    @Inject
    public void setConditionProvider(Provider<Condition> conditionProvider) {
        this.conditionProvider = conditionProvider;
    }


    public String getLockCollectionName() {
        return lockCollectionName;
    }

    @Inject
    public void setLockCollectionName(@Named(LOCK_COLLECTION) String lockCollectionName) {
        this.lockCollectionName = lockCollectionName;
    }

    public String getPeerCollectionName() {
        return peerCollectionName;
    }

    @Inject
    public void setPeerCollectionName(@Named(PEER_COLLECTION) String peerCollectionName) {
        this.peerCollectionName = peerCollectionName;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    @Inject
    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

}
