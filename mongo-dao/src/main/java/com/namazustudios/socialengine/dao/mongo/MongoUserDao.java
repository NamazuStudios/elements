package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static dev.morphia.query.experimental.updates.UpdateOperators.unset;

/**
 * MongoDB implementation of {@link UserDao}.
 *
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoUserDao implements UserDao {

    private Datastore datastore;

    private String passwordEncoding;

    private ValidationHelper validationHelper;

    private ObjectIndex objectIndex;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoPasswordUtils mongoPasswordUtils;

    private MongoProfileDao mongoProfileDao;

    @Override
    public User getActiveUser(final String userId) {
        final MongoUser mongoUser = getActiveMongoUser(userId);
        return getDozerMapper().map(mongoUser, User.class);
    }

    public MongoUser getActiveMongoUser(final User user) {
        return getActiveMongoUser(user.getId());
    }

    public MongoUser findActiveMongoUser(final String userId) {
        final ObjectId objectId = getMongoDBUtils().parseOrReturnNull(userId);
        return objectId == null ? null : getActiveMongoUser(objectId);
    }

    public MongoUser getActiveMongoUser(final String userId) {
        final ObjectId objectId = getMongoDBUtils().parseOrThrow(userId, UserNotFoundException::new);
        return getActiveMongoUser(objectId);
    }

    public MongoUser getActiveMongoUser(final ObjectId mongoUserId) {

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(and(
                eq("_id", mongoUserId)),
                eq("active", true)
        );

        final MongoUser mongoUser = query.first();

        if (mongoUser == null) {
            throw new UserNotFoundException("User with id " + mongoUserId + " not found.");
        }

        return mongoUser;

    }

    @Override
    public User getActiveUserByNameOrEmail(final String userNameOrEmail) {

        final String trimmedUserNameOrEmail = nullToEmpty(userNameOrEmail).trim();

        if (trimmedUserNameOrEmail.isEmpty()) {
            throw new InvalidDataException("name/email must be specified.");
        }

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(eq("active", true));

        query.filter(or(
                eq("name", trimmedUserNameOrEmail),
                eq("email", trimmedUserNameOrEmail)
        ));

        final MongoUser mongoUser = query.first();

        if (mongoUser == null) {
            throw new UserNotFoundException("User " + trimmedUserNameOrEmail + " not found.");
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public Pagination<User> getActiveUsers(final int offset, final int count) {
        final Query<MongoUser> query = getDatastore().find(MongoUser.class);
        query.filter(eq("active", true));
        return paginationFromQuery(query, offset, count);
    }

    @Override
    public Pagination<User> getActiveUsers(int offset, int count, String queryString) {

        final String trimmedQueryString = nullToEmpty(queryString).trim();

        if (trimmedQueryString.isEmpty()) {
            throw new InvalidDataException("queryString must be specified.");
        }

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(
                eq("active", true),
                or(
                        Filters.regex("name").pattern(Pattern.compile(queryString)),
                        Filters.regex("email").pattern(Pattern.compile(queryString))
                )
        );

        return paginationFromQuery(query, offset, count);

    }

    private Pagination<User> paginationFromQuery(final Query<MongoUser> query, final int offset, final int count) {
        return getMongoDBUtils().paginationFromQuery(query, offset, count, u -> getDozerMapper().map(u, User.class), new FindOptions());
    }

    @Override
    public User createUserStrict(final User user) {

        validate(user);

        final MongoUser mongoUser = getDozerMapper().map(user, MongoUser.class);

        mongoUser.setActive(true);
        getMongoPasswordUtils().scramblePassword(mongoUser);

        try {
            getDatastore().save(mongoUser);
            getObjectIndex().index(mongoUser);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    public User createUserWithPasswordStrict(final User user, final String password) {

        validate(user);

        final MongoUser mongoUser = getDozerMapper().map(user, MongoUser.class);
        mongoUser.setActive(true);

        final byte[] passwordBytes;

        try {
            passwordBytes = password.getBytes(getPasswordEncoding());
        } catch (UnsupportedEncodingException ex) {
            throw new InternalException(ex);
        }

        // Generate the hash

        final byte[] salt = new byte[MongoPasswordUtils.SALT_LENGTH];
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        final MessageDigest digest = getMongoPasswordUtils().newPasswordMessageDigest();
        digest.update(salt);
        digest.update(passwordBytes);

        mongoUser.setSalt(salt);
        mongoUser.setPasswordHash(digest.digest());
        mongoUser.setHashAlgorithm(digest.getAlgorithm());

        try {
            getDatastore().save(mongoUser);
            getObjectIndex().index(mongoUser);
            
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    public User createOrReactivateUserWithPassword(final User user, final String password) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class)
            .filter(or(
                eq("name", user.getName()),
                eq("email", user.getEmail())
            ))
            .filter(and(
                eq("active", false)
            ));

        final var builder = new UpdateBuilder();

        builder.with(
            set("active", true),
            set("name", user.getName()),
            set("email", user.getEmail()),
            set("level", user.getLevel())
        );

        if (user.getFacebookId() != null) builder.with(set("facebookId", user.getFacebookId()));
        if (user.getFirebaseId() != null) builder.with(set("firebaseId", user.getFacebookId()));
        if (user.getAppleSignInId() != null) builder.with(set("appleSignInId", user.getFacebookId()));

        getMongoPasswordUtils().addPasswordToBuilder(builder, password);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User createOrReactivateUser(final User user) {

        validate(user);

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(or(
            eq("name", user.getName()),
            eq("email", user.getEmail())
        )).filter(and(
            eq("active", false)
        ));

        final var builder = new UpdateBuilder()
            .with(
                set("active", true),
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            )
            .with(getMongoPasswordUtils()::scramblePassword);

        if (user.getFacebookId() == null) {
            builder.with(unset("facebookId"));
        } else {
            builder.with(set("facebookId", user.getFacebookId()));
        }

        final var opts = new ModifyOptions()
            .upsert(true)
            .returnDocument(AFTER);

        final var mongoUser = getMongoDBUtils().perform(ds -> builder.execute(query, opts));
        getObjectIndex().index(mongoUser);

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateUserStrict(final User user) {

        validate(user);

        final var objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);

        final var query = getDatastore().find(MongoUser.class).filter(and(
            eq("_id", objectId),
            eq("name", user.getName()),
            eq("email", user.getEmail())
        ));

        final var builder = new UpdateBuilder();

        if (user.getFacebookId() != null) {
            builder.with(set("facebookId", user.getFacebookId()),
                    set("active", user.isActive()),
                    set("name", user.getName()),
                    set("email", user.getEmail()),
                    set("level", user.getLevel())
            );
        } else {
            builder.with(set("active", user.isActive()),
                    set("name", user.getName()),
                    set("email", user.getEmail()),
                    set("level", user.getLevel())
            );
        }

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateUserStrict(final User user, final String password) {

        validate(user);

        final var objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);

        final var query = getDatastore().find(MongoUser.class).filter(and(
            eq("_id", objectId),
            eq("name", user.getName()),
            eq("email", user.getEmail())
        ));

        final var builder = new UpdateBuilder();

        if (user.getFacebookId() != null) {
            builder.with(
                set("facebookId", user.getFacebookId()),
                set("active", user.isActive()),
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            );
        } else {
            builder.with(
                set("active", user.isActive()),
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            );
        }

        getMongoPasswordUtils().addPasswordToBuilder(builder, password);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateActiveUser(final User user) {

        validate(user);

        final var objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);
        final var query = getDatastore().find(MongoUser.class);

        query.filter(
            and(
                eq("_id", objectId),
                eq("active", true)
            )
        );

        final var builder = new UpdateBuilder();

        if (user.getFacebookId() != null) {
            builder.with(
                set("facebookId", user.getFacebookId()),
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            );
        } else {
            builder.with(
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            );
        }

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        getObjectIndex().index(mongoUser);
        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateActiveUser(final User user, final String password) {

        validate(user);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());

        final var query = getDatastore().find(MongoUser.class).filter(
            and(
                eq("_id", objectId),
                eq("active", true)
            )
        );

        final var builder = new UpdateBuilder();

        if (user.getFacebookId() != null) {
            builder.with(
                set("facebookId", user.getFacebookId()),
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            );
        } else {
            builder.with(
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            );
        }

        getMongoPasswordUtils().addPasswordToBuilder(builder, password);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public void softDeleteUser(final String userId) {

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(userId);

        query.filter(and(
            eq("_id", objectId),
            eq("active", true)
        ));

        final var mongoUser = getMongoDBUtils().perform(ds -> new UpdateBuilder()
                .with(set("active", false))
                .with(getMongoPasswordUtils()::scramblePassword)
            .execute(query, new ModifyOptions().returnDocument(AFTER))
        );

        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with userid does not exist:" + userId);
        }

        getMongoProfileDao().softDeleteProfilesForUser(mongoUser);
    }

    public void validate(final User user) {

        if (user == null) {
            throw new InvalidDataException("User must not be null.");
        }

        getValidationHelper().validateModel(user);

        user.setEmail(nullToEmpty(user.getEmail()).trim());
        user.setName(nullToEmpty(user.getName()).trim());

    }

    @Override
    public User validateActiveUserPassword(final String userNameOrEmail, final String password) {

        final var query = getDatastore().find(MongoUser.class);

        if (ObjectId.isValid(userNameOrEmail)) {
            query.filter(eq("_id", new ObjectId(userNameOrEmail)));
            query.filter(eq("active", true));
        } else {
            query.filter(or(
                and(
                    eq("name", userNameOrEmail),
                    eq("active", true)
                ),
                and(
                    eq("email", userNameOrEmail),
                    eq("active", true)
                )
            ));
        }

        final var mongoUser = query.first();

        if (mongoUser == null) {
            throw new ForbiddenException("Invalid credentials for " + userNameOrEmail);
        }

        final byte[] passwordBytes;

        try {
            passwordBytes = password.getBytes(getPasswordEncoding());
        } catch (UnsupportedEncodingException ex) {
            throw new InternalException(ex);
        }

        final MessageDigest digest;

        try {

            if (mongoUser.getHashAlgorithm() == null) {
                throw new ForbiddenException();
            }

            final var algo = nullToEmpty(mongoUser.getHashAlgorithm());
            digest = MessageDigest.getInstance(algo);

        } catch (NoSuchAlgorithmException ex) {
            throw new ForbiddenException(ex);
        }

        digest.update(mongoUser.getSalt());
        digest.update(passwordBytes);

        final byte[] existingPasswordHash = mongoUser.getPasswordHash();

        if (existingPasswordHash != null && Arrays.equals(existingPasswordHash, digest.digest())) {
            return getDozerMapper().map(mongoUser, User.class);
        }

        throw new ForbiddenException("Invalid credentials for " + userNameOrEmail);

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public String getPasswordEncoding() {
        return passwordEncoding;
    }

    @Inject
    public void setPasswordEncoding(@Named(Constants.PASSWORD_ENCODING) String passwordEncoding) {
        this.passwordEncoding = passwordEncoding;
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

    public StandardQueryParser getStandardQueryParser() {
        return standardQueryParser;
    }

    @Inject
    public void setStandardQueryParser(StandardQueryParser standardQueryParser) {
        this.standardQueryParser = standardQueryParser;
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

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }
}
