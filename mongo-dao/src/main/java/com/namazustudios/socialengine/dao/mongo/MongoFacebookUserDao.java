package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.model.ReturnDocument;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.user.User;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.function.Function.identity;

/**
 * Created by patricktwohig on 6/25/17.
 */
public class MongoFacebookUserDao implements FacebookUserDao {

    private Datastore datastore;

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
        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(
                Filters.and(
                        Filters.eq("active", true),
                        Filters.eq("facebookId", facebookId)
                )
        );

        final MongoUser mongoUser = query.first();

        if (mongoUser == null) {
            throw new NotFoundException("User with Facebook ID " + facebookId + " not found.");
        }

        return mongoUser;
    }

    @Override
    public User connectActiveFacebookUserIfNecessary(final User user) {

        validate(user);

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        if (user.getId() == null) {
            throw new IllegalArgumentException("User must have user id.");
        }

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());

        query.filter(
                Filters.eq("_id", objectId),
                Filters.eq("active", true),
                Filters.or(
                        Filters.exists("facebookId").not(),
                        Filters.eq("facebookId", user.getFacebookId())
                )
        );
        final MongoUser mongoUser;

        try {
            mongoUser = query.modify(UpdateOperators.set("facebookId", user.getFacebookId()))
                    .execute(new ModifyOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
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

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);
        Map<String, Object> insertMap = new HashMap<>(Collections.emptyMap());

        if (user.getId() == null) {
            insertMap.put("_id", new ObjectId());
        } else {
            final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());
            query.filter(Filters.eq("_id", objectId));
        }
        
        query.filter(Filters.or(Filters.eq("facebookId", user.getFacebookId()),
                Filters.and(Filters.exists("facebookId"), Filters.eq("email", user.getEmail()))));

        insertMap.put("email", user.getEmail());
        insertMap.put("name", user.getName());
        insertMap.put("level", user.getLevel());
        insertMap.put("facebookId", user.getFacebookId());
        insertMap = getMongoPasswordUtils().scramblePasswordOnInsert(insertMap);

        // We only reactivate the existing user, all other fields are left untouched if the user exists.
        query.update(UpdateOperators.set("active", true),
        UpdateOperators.setOnInsert(insertMap)).execute(new UpdateOptions().upsert(true));

        final MongoUser mongoUser;

        try {
                mongoUser = query.first();
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

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(
                Filters.and(
                        Filters.eq("active", true),
                        Filters.in("FacebookId", facebookIds)
                )
        );

        return query.iterator().toList()
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
        user.setFacebookId(emptyToNull(nullToEmpty(user.getFacebookId()).trim()));

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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
