package dev.getelements.elements.dao.mongo;

import com.mongodb.DuplicateKeyException;
import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.model.exception.*;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;

import org.bson.types.ObjectId;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;

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

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry dozerMapperRegistry;

    private MongoPasswordUtils mongoPasswordUtils;

    private MongoProfileDao mongoProfileDao;

    private MongoUserUidDao mongoUserUidDao;

    @Override
    public User getUser(final String userId) {
        final MongoUser mongoUser = getMongoUser(userId);
        return getDozerMapper().map(mongoUser, User.class);
    }

    public MongoUser getMongoUser(final String userId) {
        return findMongoUser(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));
    }

    public MongoUser getMongoUser(final ObjectId mongoUserId) {
        return findMongoUser(mongoUserId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + mongoUserId + " not found."));
    }

    public Optional<MongoUser> findMongoUser(final String userId) {
        final var mongoUserId = getMongoDBUtils().parseOrReturnNull(userId);
        return mongoUserId == null ? Optional.empty() : findMongoUser(mongoUserId);
    }

    public Optional<MongoUser> findMongoUser(final ObjectId userId) {

        final var query = getDatastore().find(MongoUser.class);

        query.filter(and(
            eq("_id", userId))
        );

        final var mongoUser = query.first();
        return Optional.ofNullable(mongoUser);

    }

    @Override
    public Optional<User> findUserByNameOrEmail(String userNameOrEmail) {

        final var trimmedUserNameOrEmail = nullToEmpty(userNameOrEmail).trim();

        if (trimmedUserNameOrEmail.isEmpty()) throw new InvalidDataException("name/email must be specified.");

        final var query = getDatastore().find(MongoUser.class);

        query.filter(or(
            eq("name", trimmedUserNameOrEmail),
            eq("email", trimmedUserNameOrEmail)
        ));

        final var mongoUser = query.first();

        return mongoUser == null
            ? Optional.empty()
            : Optional.of(getDozerMapper().map(mongoUser, User.class));

    }

    public Optional<User> findUser(final String userId) {

        final var mongoUserId = getMongoDBUtils().parseOrReturnNull(userId);

        if(mongoUserId == null) {
            return findUserByNameOrEmail(userId);
        }

        final var mongoUser = findMongoUser(mongoUserId);

        return mongoUser.map(user -> getDozerMapper().map(user, User.class));
    }

    @Override
    public Pagination<User> getUsers(final int offset, final int count) {
        final Query<MongoUser> query = getDatastore().find(MongoUser.class);
        return paginationFromQuery(query, offset, count);
    }

    @Override
    public Pagination<User> getUsers(int offset, int count, String queryString) {

        final String trimmedQueryString = nullToEmpty(queryString).trim();

        if (trimmedQueryString.isEmpty()) {
            throw new InvalidDataException("queryString must be specified.");
        }

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        query.filter(
                or(
                        Filters.regex("name", Pattern.compile(queryString)),
                        Filters.regex("email", Pattern.compile(queryString))
                )
        );

        return paginationFromQuery(query, offset, count);

    }

    @Override
    public Pagination<User> getUsersByPrimaryPhoneNumbers(int offset, int count, List<String> phones) {
        final Query<MongoUser> query = getDatastore().find(MongoUser.class);
        query.filter(in("primaryPhoneNb", phones));
        return paginationFromQuery(query, offset, count);
    }

    private Pagination<User> paginationFromQuery(final Query<MongoUser> query, final int offset, final int count) {
        return getMongoDBUtils().paginationFromQuery(query, offset, count, u -> getDozerMapper().map(u, User.class), new FindOptions());
    }

    @Override
    public User createUserStrict(final User user) {

        validate(user);

        final MongoUser mongoUser = getDozerMapper().map(user, MongoUser.class);

        getMongoPasswordUtils().scramblePassword(mongoUser);

        try {
            getDatastore().save(mongoUser);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        final var createdUser = getDozerMapper().map(mongoUser, User.class);
        createUidsStrictForUser(createdUser);

        return createdUser;

    }

    public User createUserWithPasswordStrict(final User user, final String password) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class);

        final var existingUser = query.filter(
                        or(
                                eq("name", user.getName()),
                                eq("email", user.getEmail())
                        )
                )
                .first();

        if(existingUser != null) {
            throw new DuplicateException("A user already exists with either this name or email.");
        }

        final MongoUser mongoUser = getDozerMapper().map(user, MongoUser.class);

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

        getMongoDBUtils().performV(ds -> getDatastore().save(mongoUser));

        final var createdUser = getDozerMapper().map(mongoUser, User.class);
        createUidsStrictForUser(createdUser);

        return createdUser;

    }

    public User createUserWithPassword(final User user, final String password) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class)
            .filter(and(
                or(
                    eq("name", user.getName()),
                    eq("email", user.getEmail())
                )
            ));

        final var builder = new UpdateBuilder();

        builder.with(
            set("name", user.getName()),
            set("email", user.getEmail()),
            set("level", user.getLevel())
        );

        updateBuilderWithOptionalData(user, builder);

        getMongoPasswordUtils().addPasswordToBuilder(builder, password);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        final var createdUser = getDozerMapper().map(mongoUser, User.class);
        createUidsStrictForUser(createdUser);

        return createdUser;

    }

    @Override
    public User createUser(final User user) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class)
            .filter(or(
                eq("name", user.getName()),
                eq("email", user.getEmail())
            ));

        final var builder = new UpdateBuilder()
            .with(
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
            )
            .with(getMongoPasswordUtils()::scramblePassword);

        updateBuilderWithOptionalData(user, builder);

        final var opts = new ModifyOptions()
            .upsert(true)
            .returnDocument(AFTER);

        final var mongoUser = getMongoDBUtils().perform(ds -> builder.execute(query, opts));
        final var createdUser = getDozerMapper().map(mongoUser, User.class);
        createUidsStrictForUser(createdUser);

        return createdUser;

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

        builder.with(
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
        );

        updateBuilderWithOptionalData(user, builder);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

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

        builder.with(
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
        );

        updateBuilderWithOptionalData(user, builder);

        getMongoPasswordUtils().addPasswordToBuilder(builder, password);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateUser(final User user) {

        validate(user);

        final var objectId = getMongoDBUtils().parseOrThrow(user.getId(), UserNotFoundException::new);
        final var query = getDatastore().find(MongoUser.class);

        query.filter(
            and(
                eq("_id", objectId)
            )
        );

        final var builder = new UpdateBuilder();

        builder.with(
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
        );

        updateBuilderWithOptionalData(user, builder);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateUser(final User user, final String password) {

        validate(user);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());

        final var query = getDatastore().find(MongoUser.class).filter(
            and(
                eq("_id", objectId)
            )
        );

        final var builder = new UpdateBuilder();

        builder.with(
                set("name", user.getName()),
                set("email", user.getEmail()),
                set("level", user.getLevel())
        );

        updateBuilderWithOptionalData(user, builder);

        getMongoPasswordUtils().addPasswordToBuilder(builder, password);

        final var mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateUser(final User user, final String newPassword, final String oldPassword) {

        validate(user);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(user.getId());

        final var query = getDatastore().find(MongoUser.class).filter(
                eq("_id", objectId)
        );

        var mongoUser = query.first();

        if (mongoUser == null) {
            throw new NotFoundException("User does not exist.");
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

        final byte[] oldPasswordBytes;

        try {
            oldPasswordBytes = oldPassword.getBytes(getPasswordEncoding());
        } catch (UnsupportedEncodingException ex) {
            throw new InternalException(ex);
        }

        digest.update(mongoUser.getSalt());
        digest.update(oldPasswordBytes);
        query.filter(eq("passwordHash", digest.digest()));

        final var builder = new UpdateBuilder();
        updateBuilderWithOptionalData(user, builder);
        getMongoPasswordUtils().addPasswordToBuilder(builder, newPassword);

        mongoUser = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User does not exist.");
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User createOrUpdateUser(final User user) {

        validate(user);

        final var query = getDatastore().find(MongoUser.class)
                .filter(or(
                        eq("name", user.getName()),
                        eq("email", user.getEmail())
                ));

        final var builder = new UpdateBuilder()
                .with(
                        set("name", user.getName()),
                        set("email", user.getEmail()),
                        set("level", user.getLevel())
                )
                .with(getMongoPasswordUtils()::scramblePassword);

        updateBuilderWithOptionalData(user, builder);

        final var opts = new ModifyOptions()
                .upsert(true)
                .returnDocument(AFTER);

        final var mongoUser = getMongoDBUtils().perform(ds -> builder.execute(query, opts));
        final var createdUser = getDozerMapper().map(mongoUser, User.class);
        createUidsStrictForUser(createdUser);

        return createdUser;
    }

    @Override
    public void softDeleteUser(final String userId) {

        final Query<MongoUser> query = getDatastore().find(MongoUser.class);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(userId);

        query.filter(and(
            eq("_id", objectId)
        ));

        final var builder = new UpdateBuilder()
                .with(
                        set("name", ""),
                        set("email", ""),
                        set("primaryPhoneNb", ""),
                        set("firstName", ""),
                        set("lastName", "")
                )
                .with(getMongoPasswordUtils()::scramblePassword);

        final var mongoUser = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().returnDocument(AFTER))
        );

        if (mongoUser == null) {
            throw new NotFoundException("User with userid does not exist:" + userId);
        }

        getMongoProfileDao().softDeleteProfilesForUser(mongoUser);
        getMongoUserUidDao().softDeleteUserUidsForUserId(getDozerMapper().map(mongoUser, User.class));
    }

    private void updateBuilderWithOptionalData(User user, UpdateBuilder builder) {
        if (user.getPrimaryPhoneNb() != null) builder.with(set("primaryPhoneNb", user.getPrimaryPhoneNb()));
        if (user.getFirstName() != null) builder.with(set("firstName", user.getFirstName()));
        if (user.getLastName() != null) builder.with(set("lastName", user.getLastName()));
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
    public Optional<User> findUserWithLoginAndPassword(final String userNameOrEmail, final String password) {

        final var query = getDatastore().find(MongoUser.class);

        if (ObjectId.isValid(userNameOrEmail)) {
            query.filter(eq("_id", new ObjectId(userNameOrEmail)));
        } else {
            query.filter(
                or(
                    eq("name", userNameOrEmail),
                    eq("email", userNameOrEmail)
                )
            );
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
            return Optional.of(getDozerMapper().map(mongoUser, User.class));
        } else {
            return Optional.empty();
        }

    }

    private void createUidsStrictForUser(final User user) {
        if(user.getEmail() != null && !user.getEmail().isBlank()) {
            createUidStrict(UserUidDao.SCHEME_EMAIL, user, user.getEmail());
        }

        if(user.getName() != null && !user.getName().isBlank()) {
            createUidStrict(UserUidDao.SCHEME_NAME, user, user.getName());
        }

        if(user.getPrimaryPhoneNb() != null && !user.getPrimaryPhoneNb().isBlank()) {
            createUidStrict(UserUidDao.SCHEME_PHONE_NUMBER, user, user.getPrimaryPhoneNb());
        }
    }

    private void createUidStrict(final String scheme, final User user, final String schemeId) {
        final var uid = new UserUid();
        uid.setScheme(scheme);
        uid.setId(schemeId);
        uid.setUserId(user.getId());

        if(user.getLinkedAccounts() == null) {
            user.setLinkedAccounts(new HashSet<>());
        }

        user.getLinkedAccounts().add(scheme);

        getMongoUserUidDao().createUserUidStrict(uid);
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

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
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

    public MongoUserUidDao getMongoUserUidDao() {
        return mongoUserUidDao;
    }

    @Inject
    public void setMongoUserUidDao(MongoUserUidDao mongoUserUidDao) {
        this.mongoUserUidDao = mongoUserUidDao;
    }
}
