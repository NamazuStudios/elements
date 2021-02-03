package com.namazustudios.socialengine.dao.mongo.applesignin;

import com.mongodb.MongoException;
import com.mongodb.client.model.ReturnDocument;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.AppleSignInUserDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoPasswordUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.ValidationHelper;
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
import java.util.Map;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static dev.morphia.query.experimental.updates.UpdateOperators.setOnInsert;

public class MongoAppleSignInUserDao implements AppleSignInUserDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoPasswordUtils mongoPasswordUtils;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private MongoUserDao mongoUserDao;

    @Override
    public User createReactivateOrUpdateUser(final User user) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class);
        final var insertMap = new HashMap<String, Object>();

        if (user.getId() == null) {
            insertMap.put("_id", new ObjectId());
        } else {
            final ObjectId objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);
            query.filter(eq("_id", objectId));
        }

        query.filter(or(
            eq("appleSignInId", user.getAppleSignInId()),
            and(
                exists("appleSignInId").not(),
                eq("email", user.getEmail())
            )
        ));

        // We only reactivate the existing user, all other fields are left untouched if the user exists.
        insertMap.put("email", user.getEmail());
        insertMap.put("name", user.getName());
        insertMap.put("level", user.getLevel());
        insertMap.put("appleSignInId", user.getAppleSignInId());
        getMongoPasswordUtils().scramblePasswordOnInsert(insertMap);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            query.modify(
                set("active", true),
                setOnInsert(insertMap)
            ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User connectActiveAppleUserIfNecessary(final User user) {

        validate(user);

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        if (user.getId() == null) {
            throw new IllegalArgumentException("User must have user id.");
        }

        final ObjectId objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);

        query.filter(eq("_id", objectId));
        query.filter(eq("active", true));

        query.filter(or(
                exists("appleSignInId").not(),
                eq("appleSignInId", user.getAppleSignInId())
            )
        );

        final var mongoUser = getMongoDBUtils().perform(ds ->
            query.modify(set("appleSignInId", user.getAppleSignInId()))
                 .execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    public void validate(final User user) {

        if (user == null) {
            throw new InvalidDataException("User must not be null.");
        }

        if (user.getAppleSignInId() == null || user.getAppleSignInId().trim().isEmpty()) {
            throw new InvalidDataException("User must specify Apple Sign-In ID.");
        }

        getValidationHelper().validateModel(user);

        user.setEmail(nullToEmpty(user.getEmail()).trim());
        user.setName(nullToEmpty(user.getName()).trim());
        user.setAppleSignInId(emptyToNull(nullToEmpty(user.getAppleSignInId()).trim()));

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
