package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoCommandException;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * MongoDB implementation of {@link UserDao}.
 *
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoUserDao implements UserDao {

    private AdvancedDatastore datastore;

    private String passwordEncoding;

    private ValidationHelper validationHelper;

    private ObjectIndex objectIndex;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoPasswordUtils mongoPasswordUtils;

    @Override
    public User getActiveUser(final String userId) {
        final MongoUser mongoUser = getActiveMongoUser(userId);
        return getDozerMapper().map(mongoUser, User.class);
    }

    public MongoUser getActiveMongoUser(final String userId) {

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);

        try {
            final ObjectId objectId = getMongoDBUtils().parse(userId);
            query.and(query.criteria("_id").equal(objectId));
        } catch (NotFoundException ex) {
            query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
            );
        }

        query.and(
            query.criteria("active").equal(true)
        );

        final MongoUser mongoUser = query.get();

        if (mongoUser == null) {
            throw new NotFoundException("User with id " + userId + " not found.");
        }

        return mongoUser;

    }

    public MongoUser getActiveMongoUser(final ObjectId mongoUserId) {

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);

        query.and(query.criteria("_id").equal(mongoUserId));
        query.and(query.criteria("active").equal(true));

        final MongoUser mongoUser = query.get();

        if (mongoUser == null) {
            throw new NotFoundException("User with id " + mongoUserId + " not found.");
        }

        return mongoUser;

    }

    @Override
    public Pagination<User> getActiveUsers(int offset, int count) {
        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        query.filter("active = ", true);
        return paginationFromQuery(query, offset, count);
    }

    @Override
    public Pagination<User> getActiveUsers(int offset, int count, String queryString) {

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        try {

            final Term activeTerm = new Term("active", "true");

            booleanQueryBuilder.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(getStandardQueryParser().parse(queryString, "name"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(MongoUser.class, booleanQueryBuilder.build(), offset, count, u -> getDozerMapper().map(u, User.class));

    }

    private Pagination<User> paginationFromQuery(final Query<MongoUser> query, final int offset, final int count) {
        return getMongoDBUtils().paginationFromQuery(query, offset, count, u -> getDozerMapper().map(u, User.class));
    }

    @Override
    public User createUserStrict(User user) {

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

        return getDozerMapper().map(getDatastore().get(mongoUser), User.class);

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

    public User createOrRectivateUserWithPassword(final User user, final String password) {

        validate(user);

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);


        query.or(
            query.criteria("name").equal(user.getName()),
            query.criteria("email").equal(user.getEmail())
        );

        query.and(
            query.criteria("active").equal(false)
        );

        operations.set("active", true);
        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());

        if (user.getFacebookId() != null) {
            operations.set("facebookId", user.getFacebookId());
        }

        getMongoPasswordUtils().addPasswordToOperations(operations, password);

        try {

            final FindAndModifyOptions options = new FindAndModifyOptions()

                    .returnNew(true)
                    .upsert(true);

            final MongoUser mongoUser = getDatastore().findAndModify(query, operations, options);
            getObjectIndex().index(mongoUser);

            return getDozerMapper().map(mongoUser, User.class);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

    }

    @Override
    public User createOrReactivateUser(User user) {
        validate(user);

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);

        query.or(
            query.criteria("name").equal(user.getName()),
            query.criteria("email").equal(user.getEmail())
        ).and(
            query.criteria("active").equal(false)
        );

        operations.set("active", true);
        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());

        if (user.getFacebookId() != null) {
            operations.set("facebookId", user.getFacebookId());
        }

        getMongoPasswordUtils().scramblePassword(operations);

        try {

            final FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

            final MongoUser mongoUser = getDatastore().findAndModify(query, operations, options);
            getObjectIndex().index(mongoUser);

            return getDozerMapper().map(mongoUser, User.class);

        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

    }

    @Override
    public User updateUserStrict(User user) {

        validate(user);

        final ObjectId objectId = getMongoDBUtils().parse(user.getId());
        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);

        query.and(
            query.criteria("_id").equal(objectId),
            query.criteria("name").equal(user.getName()),
            query.criteria("email").equal(user.getEmail())
        );

        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());
        operations.set("active", user.isActive());

        if (user.getFacebookId() != null) {
            operations.set("facebookId", user.getFacebookId());
        }

        final FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoUser mongoUser = getDatastore().findAndModify(query, operations, options);
        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateUserStrict(User user, final String password) {

        validate(user);

        final ObjectId objectId = getMongoDBUtils().parse(user.getId());
        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);

        query.and(
            query.criteria("_id").equal(objectId),
            query.criteria("name").equal(user.getName()),
            query.criteria("email").equal(user.getEmail())
        );

        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());
        operations.set("active", user.isActive());

        if (user.getFacebookId() != null) {
            operations.set("facebookId", user.getFacebookId());
        }

        getMongoPasswordUtils().addPasswordToOperations(operations, password);

        final FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoUser mongoUser = getDatastore().findAndModify(query, operations, options);
        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateActiveUser(User user) {

        validate(user);

        final ObjectId objectId = getMongoDBUtils().parse(user.getId());
        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);

        query.and(
            query.criteria("_id").equal(objectId),
            query.criteria("active").equal(true)
        );

        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());

        if (user.getFacebookId() != null) {
            operations.set("facebookId", user.getFacebookId());
        }

        final MongoUser mongoUser = getDatastore().findAndModify(query, operations);
        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public User updateActiveUser(User user, String password) {

        validate(user);

        final ObjectId objectId = getMongoDBUtils().parse(user.getId());
        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);

        query.and(
            query.criteria("_id").equal(objectId),
            query.criteria("active").equal(true)
        );

        operations.set("name", user.getName());
        operations.set("email", user.getEmail());
        operations.set("level", user.getLevel());

        if (user.getFacebookId() != null) {
            operations.set("facebookId", user.getFacebookId());
        }

        getMongoPasswordUtils().addPasswordToOperations(operations, password);

        final MongoUser mongoUser = getDatastore().findAndModify(query, operations);
        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with email/username does not exist: " +  user.getEmail() + "/" + user.getName());
        }

        return getDozerMapper().map(mongoUser, User.class);

    }

    @Override
    public void softDeleteUser(String userId) {


        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);

        try {
            final ObjectId objectId = getMongoDBUtils().parse(userId);
            query.and(query.criteria("_id").equal(objectId));
        } catch (NotFoundException ex) {
            query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
            );
        }

        query.and(
            query.criteria("active").equal(true)
        );

        operations.set("active", false);
        getMongoPasswordUtils().scramblePassword(operations);

        final MongoUser mongoUser = getDatastore().findAndModify(query, operations);
        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with userid does not exist:" + userId);
        }

    }

    @Override
    public User updateActiveUserPassword(String userId, String password) {

        password = nullToEmpty(password).trim();

        if (isNullOrEmpty(password)) {
            throw new InvalidDataException("Password must not be blank.");
        }

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);
        final UpdateOperations<MongoUser> operations = getDatastore().createUpdateOperations(MongoUser.class);

        try {
            final ObjectId objectId = getMongoDBUtils().parse(userId);
            query.and(query.criteria("_id").equal(objectId));
        } catch (NotFoundException ex) {
            query.or(
                query.criteria("name").equal(userId),
                query.criteria("email").equal(userId)
            );
        }

        query.and(
            query.criteria("active").equal(true)
        );

        // Generate the password bytes from the encoding

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

        operations.set("salt", salt);
        operations.set("passwordHash", digest.digest());
        operations.set("hashAlgorithm", digest.getAlgorithm());

        final MongoUser mongoUser = getDatastore().findAndModify(query, operations);
        getObjectIndex().index(mongoUser);

        if (mongoUser == null) {
            throw new NotFoundException("User with userid does not exist:" + userId);
        }

        return getDozerMapper().map(mongoUser, User.class);
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
    public User validateActiveUserPassword(String userId, String password) {

        final Query<MongoUser> query = getDatastore().createQuery(MongoUser.class);

        query.or(
            query.criteria("name").equal(userId),
            query.criteria("email").equal(userId)
        ).and(
            query.criteria("active").equal(true)
        );

        final MongoUser mongoUser = query.get();

        if (mongoUser == null) {
            throw new ForbiddenException("Invalid credentials for " + userId);
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

            final String algo = nullToEmpty(mongoUser.getHashAlgorithm());
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

        throw new ForbiddenException("Invalid credentials for " + userId);

    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
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

}
