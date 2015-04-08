package com.namazustudios.promotion.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.namazustudios.promotion.Constants;
import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.dao.mongo.model.MongoUser;
import com.namazustudios.promotion.exception.ForbiddenException;
import com.namazustudios.promotion.exception.InternalException;
import com.namazustudios.promotion.exception.InvalidDataException;
import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.User;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoUserDao implements UserDao {

    private static final int SALT_LENGTH = 12;

    @Inject
    private Datastore datastore;

    @Inject
    @Named(Constants.QUERY_MAX_RESULTS)
    private int queryMaxResults;

    @Inject
    @Named(Constants.PASSWORD_DIGEST)
    private Provider<MessageDigest> messageDigestProvider;

    @Inject
    @Named(Constants.PASSWORD_ENCODING)
    private String passwordEncoding;

    @Override
    public User getUser(String userId) {

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);

        query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
        ).and(
                query.criteria("active").equal(true)
        );

        final MongoUser mongoUser = query.get();
        return transform(mongoUser);

    }

    @Override
    public Pagination<User> getUsers(int offset, int count) {

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);

        query.filter("active =", true);
        query.offset(offset).limit(Math.min(queryMaxResults, count));

        final Pagination<User> users = new Pagination<>();

        users.setOffset(offset);
        users.setTotal((int)query.getCollection().getCount());

        final Iterable<User> userIterable = Iterables.transform(query, new Function<MongoUser, User>() {
            @Override
            public User apply(MongoUser input) {
                return transform(input);
            }
        });

        users.setObjects(Lists.newArrayList(userIterable));

        return users;

    }

    @Override
    public User createUser(User user) {

        validate(user);

        final MongoUser mongoUser = new MongoUser();
        final SecureRandom secureRandom = new SecureRandom();

        mongoUser.setActive(true);
        mongoUser.setName(user.getName());
        mongoUser.setEmail(user.getEmail());
        mongoUser.setLevel(user.getLevel());

        byte[] tmp;

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        mongoUser.setSalt(tmp);

        tmp = new byte[SALT_LENGTH];
        secureRandom.nextBytes(tmp);
        mongoUser.setPasswordHash(tmp);

        datastore.save(mongoUser);
        return transform(mongoUser);

    }

    @Override
    public User updateUser(User user) {

        validate(user);

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = datastore.createUpdateOperations(MongoUser.class);

        query.and(
                query.criteria("name").equal(user.getName()),
                query.criteria("email").equal(user.getEmail())
        ).and(
                query.criteria("active").equal(true)
        );

        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());

        final MongoUser mongoUser = datastore.findAndModify(query, operations);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return transform(mongoUser);
    }

    @Override
    public void deleteUser(String userId) {

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = datastore.createUpdateOperations(MongoUser.class);

        query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
        ).and(
                query.criteria("active").equal(true)
        );

        operations.set("active", false);

        final MongoUser mongoUser = datastore.findAndModify(query, operations);

        if (mongoUser == null) {
            throw new NotFoundException("User with userid does not exist:" + userId);
        }

    }

    @Override
    public User updateUserPassword(String userId, String password) {

        password = Strings.nullToEmpty(password).trim();

        if (Strings.isNullOrEmpty(password)) {
            throw new InvalidDataException("Password must not be blank.");
        }

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = datastore.createUpdateOperations(MongoUser.class);

        query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
        ).and(
                query.criteria("active").equal(true)
        );

        // Generate the password bytes from the encoding

        final byte[] passwordBytes;

        try {
            passwordBytes = password.getBytes(passwordEncoding);
        } catch (UnsupportedEncodingException ex) {
            throw new InternalException(ex);
        }

        // Generate the hash

        final byte[] salt = new byte[SALT_LENGTH];
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        final MessageDigest digest = messageDigestProvider.get();
        digest.update(salt);
        digest.update(passwordBytes);

        operations.set("salt", salt);
        operations.set("password_hash", digest.digest());

        final MongoUser mongoUser = datastore.findAndModify(query, operations);

        if (mongoUser == null) {
            throw new NotFoundException("User with userid does not exist:" + userId);
        }

        return transform(mongoUser);
    }

    private User transform(final MongoUser mongoUser) {

        final User user = new User();

        user.setName(mongoUser.getObjectId());
        user.setEmail(mongoUser.getEmail());
        user.setLevel(mongoUser.getLevel());

        return user;

    }

    public void validate(final User user) {

        if (user == null) {
            throw new InvalidDataException("User must not be null.");
        }

        user.setEmail(Strings.nullToEmpty(user.getEmail()).trim());
        user.setName(Strings.nullToEmpty(user.getName()).trim());

        if (Strings.isNullOrEmpty(user.getEmail())) {
            throw new InvalidDataException("Email must not be null.");
        }

        if (Strings.isNullOrEmpty(user.getName())) {
            throw new InvalidDataException("User name must not be null.");
        }

        if (user.getLevel() == null) {
            throw new InvalidDataException("User level must be specified.");
        }

    }

    @Override
    public User validateUserPassword(String userId, String password) {

        final Query<MongoUser> query = datastore.createQuery(MongoUser.class);

        query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
        ).and(
                query.criteria("active").equal(true)
        );

        final MongoUser mongoUser = query.get();

        final byte[] passwordBytes;

        try {
            passwordBytes = password.getBytes(passwordEncoding);
        } catch (UnsupportedEncodingException ex) {
            throw new InternalException(ex);
        }

        final MessageDigest digest = messageDigestProvider.get();
        digest.update(mongoUser.getSalt());
        digest.update(passwordBytes);

        final byte[] existingPasswordHash = mongoUser.getPasswordHash();

        if (existingPasswordHash != null && Arrays.equals(existingPasswordHash, digest.digest())) {
            return transform(mongoUser);
        }

        throw new ForbiddenException();

    }
}
