package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.FacebookFriendDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoFriendship;
import com.namazustudios.socialengine.dao.mongo.model.MongoFriendshipId;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.friend.FacebookFriend;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class MongoFacebookFriendDao implements FacebookFriendDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoFacebookFriendDao.class);

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoFacebookUserDao mongoFacebookUserDao;

    private ObjectIndex objectIndex;

    @Override
    public void associateFriends(final User user, final List<String> facebookIds) {
        try {
            doAssociateFriends(user, facebookIds);
        } catch (Exception ex) {
            logger.error("Failed to update one or more friends.", ex);
        }
    }

    private void doAssociateFriends(final User user, final List<String> facebookIds) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());

        for (final String facebookId : facebookIds) {

            final MongoUser friend;

            try {
                friend = getMongoFacebookUserDao().findActiveMongoUserByFacebookId(facebookId);
            } catch (NotFoundException ex) {
                continue;
            }

            final MongoFriendshipId mongoFriendshipId;
            mongoFriendshipId = new MongoFriendshipId(mongoUser.getObjectId(), friend.getObjectId());

            final UpdateOperations<MongoFriendship> update;
            update = getDatastore().createUpdateOperations(MongoFriendship.class);

            update.set("_id", mongoFriendshipId);
            update.set("lesserAccepted", true);
            update.set("greaterAccepted", true);

            final Query<MongoFriendship> query = getDatastore().createQuery(MongoFriendship.class);
            query.field("_id").equal(mongoFriendshipId);

            final UpdateResults r = getDatastore().update(query, update, new UpdateOptions().upsert(true));
            logger.debug("Updated {}.  Inserted {}", r.getUpdatedCount(), r.getInsertedCount());

            getObjectIndex().index(query.get());

        }
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoFacebookUserDao getMongoFacebookUserDao() {
        return mongoFacebookUserDao;
    }

    @Inject
    public void setMongoFacebookUserDao(MongoFacebookUserDao mongoFacebookUserDao) {
        this.mongoFacebookUserDao = mongoFacebookUserDao;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

}
