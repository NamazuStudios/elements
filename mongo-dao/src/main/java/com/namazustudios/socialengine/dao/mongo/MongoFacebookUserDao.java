package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Strings;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.namazustudios.socialengine.dao.mongo.model.MongoFriendship;
import com.namazustudios.socialengine.dao.mongo.model.MongoFriendshipId;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.User;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.function.Function.identity;

/**
 * Created by patricktwohig on 6/25/17.
 */
public class MongoFacebookUserDao implements FacebookUserDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoFacebookUserDao.class);

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoPasswordUtils mongoPasswordUtils;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private MongoUserDao mongoUserDao;

    @Override
    public User findActiveByFacebookId(final String facebookId) {
        final MongoUser mongoUser = findActiveMongoUserByFacebookId(facebookId);
        return getDozerMapper().map(mongoUser, User.class);
    }

    public MongoUser findActiveMongoUserByFacebookId(final String facebookId) {
        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("facebookId").equal(facebookId)
        );

        final MongoUser mongoUser = query.get();

        if (mongoUser == null) {
            throw new NotFoundException("User with Facebook ID " + facebookId + " not found.");
        }

        return mongoUser;
    }

    @Override
    public User connectActiveFacebookUserIfNecessary(final User user) {

        validate(user);

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> updates = getDatastore().createUpdateOperations(MongoUser.class);

        if (user.getId() == null) {
            throw new IllegalArgumentException("User must have user id.");
        }

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());

        query.field("_id").equal(objectId);
        query.field("active").equal(true);

        query.or(
            query.criteria("facebookId").doesNotExist(),
            query.criteria("facebookId").equal(user.getFacebookId())
        );

        updates.set("facebookId", user.getFacebookId());

        final MongoUser mongoUser;

        try {
            mongoUser = getDatastore().findAndModify(query, updates, new FindAndModifyOptions()
                .upsert(true)
                .returnNew(true));
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User createReactivateOrUpdateUser(User user) {

        validate(user);

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> updates = getDatastore().createUpdateOperations(MongoUser.class);

        if (user.getId() == null) {
            updates.setOnInsert("_id", new ObjectId());
        } else {
            final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());
            query.field("_id").equal(objectId);
        }

        query.or(

            // Either we find a user with the supplied FacebookID.
            query.criteria("facebookId").equal(user.getFacebookId()),

            // Or we find a user who has no Facebook ID and the email does match.
            query.and(
                query.criteria("facebookId").doesNotExist(),
                query.criteria("email").equal(user.getEmail())
            )

        );

        // We only reactivate the existing user, all other fields are left untouched if the user exists.
        updates.set("active", true);

        updates.setOnInsert("email", user.getEmail());
        updates.setOnInsert("name", user.getName());
        updates.setOnInsert("level", user.getLevel());
        updates.setOnInsert("facebookId", user.getFacebookId());

        getMongoPasswordUtils().scramblePasswordOnInsert(updates);

        final MongoUser mongoUser;

        try {
            mongoUser = getDatastore().findAndModify(query, updates, new FindAndModifyOptions()
                .upsert(true)
                .returnNew(true));
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public Map<String, User> findActiveUsersWithFacebookIds(final List<String> facebookIds) {

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("facebookId").in(facebookIds)
        );

        return query.asList()
            .stream()
            .map(u -> getDozerMapper().map(u, User.class))
            .collect(Collectors.toMap(u -> u.getFacebookId(), identity()));

    }

    public void validate(final User user) {

        if (user == null) {
            throw new InvalidDataException("User must not be null.");
        }

        if (user.getFacebookId() == null || user.getFacebookId().trim().isEmpty()) {
            throw new InvalidDataException("User must specify Facebook ID.");
        }

        getValidationHelper().validateModel(user);

        user.setEmail(nullToEmpty(user.getEmail()).trim());
        user.setName(nullToEmpty(user.getName()).trim());
        user.setFacebookId(nullToEmpty(user.getFacebookId()));

    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public MongoPasswordUtils getMongoPasswordUtils() {
        return mongoPasswordUtils;
    }

    @Inject
    public void setMongoPasswordUtils(MongoPasswordUtils mongoPasswordUtils) {
        this.mongoPasswordUtils = mongoPasswordUtils;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }
}
