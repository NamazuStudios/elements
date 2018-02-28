package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.bson.types.ObjectId;
import org.mongodb.morphia.AdvancedDatastore;

import javax.inject.Inject;

public class MongoSessionDao implements SessionDao {

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    @Override
    public Session getBySessionId(final String sessionId) {
        final ObjectId objectId = new ObjectId();

        return null;
    }

    @Override
    public SessionCreation create(final Session session) {
        return null;
    }

    @Override
    public void delete(final String sessionSecret) {

    }

    @Override
    public void deleteAllSessionsForUser(final String userId) {

    }

    @Override
    public void deleteSessionForUser(final String sessionSecret, final String userId) {

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

}
