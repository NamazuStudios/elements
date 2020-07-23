package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.WriteResult;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoSession;
import com.namazustudios.socialengine.dao.mongo.model.MongoSessionSecret;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.*;

import com.namazustudios.socialengine.exception.security.BadSessionSecretException;
import com.namazustudios.socialengine.exception.security.NoSessionException;
import com.namazustudios.socialengine.exception.security.SessionExpiredException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.util.ValidationHelper;
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
import javax.inject.Named;
import javax.inject.Provider;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

public class MongoSessionDao implements SessionDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoSessionDao.class);

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private MongoUserDao mongoUserDao;

    private Provider<MessageDigest> messageDigestProvider;

    private Mapper mapper;

    @Override
    public Session getBySessionSecret(final String sessionSecret) {

        final ObjectId mongoUserId;
        final MongoSessionSecret mongoSessionSecret;

        try {
            mongoSessionSecret = new MongoSessionSecret(sessionSecret);
            mongoUserId = mongoSessionSecret.getContextAsObjectId();
        } catch (IllegalArgumentException ex) {
            throw new BadSessionSecretException(ex, "Bad Session Secret");
        }

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(mongoUserId);
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final Timestamp now = new Timestamp(currentTimeMillis());
        final Query<MongoSession> query = getDatastore().createQuery(MongoSession.class);
        query.and(query.criteria("_id").equal(sessionId));

        final MongoSession mongoSession = query.get();

        if (mongoSession == null) {
            throw new NoSessionException("Session not valid.");
        } else if (mongoSession.getExpiry().before(now)) {
            throw new SessionExpiredException("Session expired.");
        }

        return getMapper().map(mongoSession, Session.class);

    }

    @Override
    public Session refresh(final String sessionSecret, final long expiry) {

        final ObjectId mongoUserId;
        final MongoSessionSecret mongoSessionSecret;

        try {
            mongoSessionSecret = new MongoSessionSecret(sessionSecret);
            mongoUserId = mongoSessionSecret.getContextAsObjectId();
        } catch (IllegalArgumentException ex) {
            throw new BadSessionSecretException(ex, "Bad Session Secret");
        }

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(mongoUserId);
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final Timestamp now = new Timestamp(currentTimeMillis());
        final Query<MongoSession> query = getDatastore().createQuery(MongoSession.class);

        query.and(
            query.criteria("_id").equal(sessionId),
            query.criteria("expiry").greaterThan(now)
        );

        final UpdateOperations<MongoSession> updates = getDatastore().createUpdateOperations(MongoSession.class);
        updates.set("expiry", new Timestamp(expiry));
        getDatastore().update(query, updates);

        final UpdateResults updateResults;
        updateResults = getDatastore().update(query, updates, new UpdateOptions().multi(false).upsert(false));

        if (updateResults.getUpdatedCount() == 0) {

            final MongoSession mongoSession = getDatastore().get(MongoSession.class, sessionId);

            if (mongoSession == null) {
                throw new NoSessionException("Session not valid.");
            } else if (mongoSession.getExpiry().before(now)) {
                throw new SessionExpiredException("Session expired.");
            } else {
                throw new InternalException("Unable to refresh session.");
            }

        } else {
            final MongoSession mongoSession = getDatastore().get(MongoSession.class, sessionId);
            if (mongoSession.getProfile() != null) {
                updateProfileLastLogin(mongoSession.getProfile().getObjectId().toString());
            }
            return getMapper().map(mongoSession, Session.class);
        }

    }

    @Override
    public SessionCreation create(final Session session) {

        validate(session);

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(session.getUser());
        final MongoSessionSecret mongoSessionSecret = new MongoSessionSecret(mongoUser.getObjectId());

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final MongoSession mongoSession = getMapper().map(session, MongoSession.class);
        mongoSession.setSessionId(sessionId);

        getDatastore().save(mongoSession);
        if (session.getProfile() != null) updateProfileLastLogin(session.getProfile().getId());

        final SessionCreation sessionCreation = new SessionCreation();
        sessionCreation.setSessionSecret(mongoSessionSecret.getSessionSecret());
        sessionCreation.setSession(getMapper().map(mongoSession, Session.class));
        return sessionCreation;

    }

    private boolean updateProfileLastLogin(String profileId) {
        try {
            final UpdateOperations<MongoProfile> updateOperations =
                    getDatastore().createUpdateOperations(MongoProfile.class);

            final Date nowDate = new Date();
            updateOperations.set("lastLogin", nowDate);

            final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);
            query.field("_id").equal(new ObjectId(profileId));

            final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
                final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                        .upsert(false)
                        .returnNew(true);

                return ds.findAndModify(query, updateOperations, findAndModifyOptions);
            });

            if (mongoProfile == null) {
                logger.error("Failed to save lastLogin to profile (no record matching id)");
                return false;
            }
            else {
                return true;
            }
        }
        catch (MongoCommandException ex) {
            logger.error("Failed to save lastLogin to profile: {}", ex.toString());
            return false;
        }
    }

    @Override
    public void delete(final String userId, final String sessionSecret) {

        final ObjectId mongoUserId;
        final MongoSessionSecret mongoSessionSecret;

        try {
            mongoSessionSecret = new MongoSessionSecret(sessionSecret);
            mongoUserId = mongoSessionSecret.getContextAsObjectId();
        } catch (IllegalArgumentException ex) {
            throw new BadSessionSecretException(ex, "Bad Session Secret");
        }

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(mongoUserId);
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final Query<MongoSession> query = getDatastore().createQuery(MongoSession.class);

        query.field("_id").equal(sessionId)
             .field("user").equal(mongoUser);

        final WriteResult wr = getDatastore().delete(query);

        if (wr.getN() == 0) {
            throw new NotFoundException("Session Not Found.");
        } else if (wr.getN() > 1) {
            logger.error("Deleted more than one session: {}", wr.getN());
        }

    }

    @Override
    public void deleteAllSessionsForUser(final String userId) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(userId);
        final Query<MongoSession> query = getDatastore().createQuery(MongoSession.class);

        query.field("user").equal(mongoUser);
        getDatastore().delete(query);

    }

    public void validate(final Session session) {

        getValidationHelper().validateModel(session);

        final Timestamp now = new Timestamp(currentTimeMillis());
        final Timestamp expiry = new Timestamp(session.getExpiry());

        if (expiry.before(now)) {
            throw new InvalidDataException("Expiry must be in the future.");
        }

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

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public Provider<MessageDigest> getMessageDigestProvider() {
        return messageDigestProvider;
    }

    @Inject
    public void setMessageDigestProvider(@Named(Constants.PASSWORD_DIGEST) Provider<MessageDigest> messageDigestProvider) {
        this.messageDigestProvider = messageDigestProvider;
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

}
