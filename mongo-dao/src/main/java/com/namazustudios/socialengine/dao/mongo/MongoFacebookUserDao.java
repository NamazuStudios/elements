package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.FacebookUserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static dev.morphia.query.experimental.updates.UpdateOperators.setOnInsert;
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

        final var query = getDatastore().find(MongoUser.class);

        query.filter(
            and(
                eq("active", true),
                eq("facebookId", facebookId)
            )
        );

        final MongoUser mongoUser = query.first();

        if (mongoUser == null) {
            throw new NotFoundException("User with Facebook ID " + facebookId + " not found.");
        }

        return mongoUser;
    }

    @Override
    public User connectActiveUserIfNecessary(final User user) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class);

        if (user.getId() == null) {
            throw new IllegalArgumentException("User must have user id.");
        }

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());

        query.filter(
            eq("_id", objectId),
            eq("active", true),
            or(
                exists("facebookId").not(),
                eq("facebookId", user.getFacebookId())
            )
        );

        final var mongoUser = getMongoDBUtils().perform(ds ->
            query.modify(set("facebookId", user.getFacebookId()))
                 .execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new UserNotFoundException("No matching user found.");
        }

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User createReactivateOrUpdateUser(User user) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class);

        query.filter(
            or(
                eq("email", user.getEmail()),
                eq("facebookId", user.getFacebookId())
            )
        );

        final var insertMap = getMongoPasswordUtils().scramblePasswordOnInsert();

        insertMap.put("_id", new ObjectId());
        insertMap.put("name", user.getName());
        insertMap.put("email", user.getEmail());
        insertMap.put("facebookId", user.getFacebookId());
        insertMap.put("level", user.getLevel());

        // We only reactivate the existing user, all other fields are left untouched if the user exists.

        final var mongoUser = getMongoDBUtils().perform(db -> query.modify(
                set("active", true),
                setOnInsert(insertMap)
            ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new UserNotFoundException("No matching user found.");
        }

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public Map<String, User> findActiveUsersWithFacebookIds(final List<String> facebookIds) {

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(
                and(
                        eq("active", true),
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
