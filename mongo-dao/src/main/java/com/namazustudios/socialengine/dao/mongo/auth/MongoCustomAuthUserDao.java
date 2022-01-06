package com.namazustudios.socialengine.dao.mongo.auth;

import com.namazustudios.socialengine.dao.CustomAuthUserDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoPasswordUtils;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.auth.UserClaim;
import com.namazustudios.socialengine.model.auth.UserKey;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoCustomAuthUserDao implements CustomAuthUserDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    private MongoPasswordUtils mongoPasswordUtils;

    @Override
    public Optional<User> findActiveUser(final UserKey userKey, final String subject) {
        return findMongoUser(userKey, subject).map(o -> getBeanMapper().map(o, User.class));
    }

    public Optional<MongoUser> findMongoUser(final UserKey userKey, final String subject) {

        final var mongoUser = getDatastore()
            .find(MongoUser.class)
            .filter(eq("active", true))
            .filter(eq(userKey.getValue(), subject))
            .first();

        return Optional.ofNullable(mongoUser);

    }

    @Override
    public User upsertUser(final UserKey userKey, final String subject, final UserClaim userClaim) {

        getValidationHelper().validateModel(userClaim, Insert.class, Update.class);

        final var query = getDatastore()
            .find(MongoUser.class)
            .filter(eq(userKey.getValue(), subject));

        final var builder = new UpdateBuilder().with(
            set("active", true),
            set("name", userClaim.getName()),
            set("email", userClaim.getEmail()),
            set("level", userClaim.getLevel())
        );

        if (userClaim.getFacebookId() != null)
            builder.with(set("facebookId", userClaim.getFacebookId()));

        if (userClaim.getFirebaseId() != null)
            builder.with(set("firebaseId", userClaim.getFirebaseId()));

        if (userClaim.getAppleSignInId() != null)
            builder.with(set("appleSignInId", userClaim.getAppleSignInId()));

        if (userClaim.getExternalUserId() != null)
            builder.with(set("externalUserId", userClaim.getExternalUserId()));

        getMongoPasswordUtils().scramblePasswordOnInsert(builder);

        final var options = new ModifyOptions()
            .upsert(true)
            .returnDocument(AFTER);

        final var mongoUser = builder
            .modify(query)
            .execute(options);

        return getBeanMapper().map(mongoUser, User.class);

    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoPasswordUtils getMongoPasswordUtils() {
        return mongoPasswordUtils;
    }

    @Inject
    public void setMongoPasswordUtils(MongoPasswordUtils mongoPasswordUtils) {
        this.mongoPasswordUtils = mongoPasswordUtils;
    }

}
