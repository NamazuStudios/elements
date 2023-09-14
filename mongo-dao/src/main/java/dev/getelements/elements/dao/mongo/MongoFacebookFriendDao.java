package dev.getelements.elements.dao.mongo;

import com.mongodb.client.model.ReturnDocument;
import dev.getelements.elements.dao.FacebookFriendDao;
import dev.getelements.elements.dao.mongo.model.MongoFriendship;
import dev.getelements.elements.dao.mongo.model.MongoFriendshipId;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.user.User;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoFacebookFriendDao implements FacebookFriendDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoFacebookFriendDao.class);

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoFacebookUserDao mongoFacebookUserDao;

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

            final var query = getDatastore().find(MongoFriendship.class);

            final var mongoFriendship = query
                .filter(eq("_id", mongoFriendshipId))
                .modify(set("_id", mongoFriendshipId),
                        set("lesserAccepted", true),
                        set("greaterAccepted", true))
                .execute(new ModifyOptions().upsert(true).returnDocument(ReturnDocument.AFTER));

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

}
