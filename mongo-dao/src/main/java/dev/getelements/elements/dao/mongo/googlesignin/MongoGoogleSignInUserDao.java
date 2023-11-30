package dev.getelements.elements.dao.mongo.googlesignin;

import dev.getelements.elements.dao.GoogleSignInUserDao;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoPasswordUtils;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.user.UserNotFoundException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.HashMap;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.setOnInsert;

public class MongoGoogleSignInUserDao implements GoogleSignInUserDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

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
                eq("googleSignInId", user.getGoogleSignInId()),
                and(
                        exists("googleSignInId").not(),
                        eq("email", user.getEmail())
                )
        ));

        // We only reactivate the existing user, all other fields are left untouched if the user exists.
        insertMap.put("email", user.getEmail());
        insertMap.put("name", user.getName());
        insertMap.put("level", user.getLevel());
        insertMap.put("googleSignInId", user.getGoogleSignInId());
        getMongoPasswordUtils().scramblePasswordOnInsert(insertMap);

        final var mongoUser = getMongoDBUtils().perform(ds ->
                query.modify(
                        set("active", true),
                        setOnInsert(insertMap)
                ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User connectActiveUserIfNecessary(final User user) {

        validate(user);

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        if (user.getId() == null) {
            throw new IllegalArgumentException("User must have user id.");
        }

        final ObjectId objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);

        query.filter(eq("_id", objectId));
        query.filter(eq("active", true));

        query.filter(or(
                        exists("googleSignInId").not(),
                        eq("googleSignInId", user.getGoogleSignInId())
                )
        );

        final var mongoUser = getMongoDBUtils().perform(ds ->
                query.modify(set("googleSignInId", user.getGoogleSignInId()))
                        .execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return getDozerMapper().map(mongoUser, User.class);

    }

    public void validate(final User user) {

        if (user == null) {
            throw new InvalidDataException("User must not be null.");
        }

        if (user.getGoogleSignInId() == null || user.getGoogleSignInId().trim().isEmpty()) {
            throw new InvalidDataException("User must specify Google Sign-In ID.");
        }

        getValidationHelper().validateModel(user);

        user.setEmail(nullToEmpty(user.getEmail()).trim());
        user.setName(nullToEmpty(user.getName()).trim());
        user.setGoogleSignInId(emptyToNull(nullToEmpty(user.getGoogleSignInId()).trim()));

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
