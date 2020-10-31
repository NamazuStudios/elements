package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.WriteResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
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
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Date;

import static com.namazustudios.socialengine.dao.mongo.model.MongoSession.Type.STANDARD_ELEMENTS;
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
        final Query<MongoSession> query = getDatastore().find(MongoSession.class);

        query.filter(Filters.and(Filters.eq("_id", sessionId)));

        final MongoSession mongoSession = query.first();

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
        final Query<MongoSession> query = getDatastore().find(MongoSession.class);

        query.filter(Filters.and(
                Filters.eq("_id", sessionId)),
                Filters.gte("expiry", now)
                );

        final UpdateResult updateResult = query.update(UpdateOperators.set("expiry", new Timestamp(expiry)))
                .execute(new UpdateOptions().upsert(false));;

        final MongoSession mongoSession = getDatastore().find(MongoSession.class)
                .filter(Filters.eq("_id", sessionId)).first();

        if (updateResult.getModifiedCount() == 0) {

            if (mongoSession == null) {
                throw new NoSessionException("Session not valid.");
            } else if (mongoSession.getExpiry() != null && mongoSession.getExpiry().before(now)) {
                throw new SessionExpiredException("Session expired.");
            }

        } else {

            if (mongoSession.getProfile() != null) {
                updateProfileLastLogin(mongoSession.getProfile().getObjectId().toString());
            }

        }

        return getMapper().map(mongoSession, Session.class);

    }

    @Override
    public SessionCreation create(final Session session) {

        validate(session);

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(session.getUser());
        final MongoSessionSecret mongoSessionSecret = new MongoSessionSecret(mongoUser.getObjectId());

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final MongoSession mongoSession = getMapper().map(session, MongoSession.class);
        mongoSession.setType(STANDARD_ELEMENTS);
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
            final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);
            query.filter(Filters.eq("_id", new ObjectId(profileId)));

            final Date nowDate = new Date();
            query.update(UpdateOperators.set("lastLogin", nowDate)).execute(new UpdateOptions().upsert(false));

            final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
                return query.first();
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

        final Query<MongoSession> query = getDatastore().find(MongoSession.class);

        query.filter(Filters.eq("_id", sessionId))
             .filter(Filters.eq("user", mongoUser));

        final DeleteResult dr = query.delete();

        if (dr.getDeletedCount() == 0) {
            throw new NotFoundException("Session Not Found.");
        } else if (dr.getDeletedCount() > 1) {
            logger.error("Deleted more than one session: {}", dr.getDeletedCount());
        }

    }

    @Override
    public void deleteAllSessionsForUser(final String userId) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(userId);
        final Query<MongoSession> query = getDatastore().find(MongoSession.class);

        query.filter(Filters.eq("user", mongoUser));
        query.delete();
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
