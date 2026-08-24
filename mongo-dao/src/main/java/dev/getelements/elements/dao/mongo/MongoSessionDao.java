package dev.getelements.elements.dao.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.MongoSession;
import dev.getelements.elements.dao.mongo.model.MongoSessionSecret;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.security.BadSessionSecretException;
import dev.getelements.elements.sdk.model.exception.security.NoSessionException;
import dev.getelements.elements.sdk.model.exception.security.SessionExpiredException;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.function.Consumer;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.dao.mongo.model.MongoSession.Type.STANDARD_ELEMENTS;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.System.currentTimeMillis;

public class MongoSessionDao implements SessionDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoSessionDao.class);

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private Provider<MessageDigest> messageDigestProvider;

    private MapperRegistry mapperRegistry;

    private Consumer<Event> eventPublisher;

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
        final MongoUser mongoUser = getMongoUserDao().getMongoUser(mongoUserId);
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final Timestamp now = new Timestamp(currentTimeMillis());
        final Query<MongoSession> query = getDatastore().find(MongoSession.class);

        query.filter(and(eq("_id", sessionId)));

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

        final var md = getMessageDigestProvider().get();
        final var mongoUser = getMongoUserDao().getMongoUser(mongoUserId);
        final var sessionId = mongoSessionSecret.getSecretDigestEncoded(md, mongoUser.getPasswordHash());

        final var now = new Timestamp(currentTimeMillis());

        final var query = getDatastore().find(MongoSession.class).filter(
            and(
                gte("expiry", now),
                eq("_id", sessionId)
            )
        );

        final var opts = new ModifyOptions().upsert(false).returnDocument(AFTER);

        final var mongoSession = query.modify(
            set("expiry", new Timestamp(expiry))
        ).execute(opts);

        if (mongoSession == null) {
            throw new NoSessionException("Session not valid.");
        } else if (mongoSession.getExpiry() != null && mongoSession.getExpiry().before(now)) {
            throw new SessionExpiredException("Session expired.");
        }

        if (mongoSession.getProfile() != null) {
            final var profileId = mongoSession.getProfile().getObjectId();
            updateProfileLastLogin(profileId, now);
        }

        final var updatedSession = getMapper().map(mongoSession, Session.class);

        getEventPublisher().accept(Event.builder()
                .argument(updatedSession)
                .named(SESSION_UPDATED)
                .build());

        return updatedSession;

    }

    @Override
    public SessionCreation create(final Session session) {

        validate(session);

        final var mongoUser = getMongoUserDao().getMongoUser(session.getUser().getId());
        final var mongoSessionSecret = new MongoSessionSecret(mongoUser.getObjectId());

        final var messageDigest = getMessageDigestProvider().get();
        final var sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final var mongoSession = getMapper().map(session, MongoSession.class);
        mongoSession.setType(STANDARD_ELEMENTS);
        mongoSession.setSessionId(sessionId);
        getDatastore().save(mongoSession);

        if (session.getProfile() != null) {
            final var profileId = mongoSession.getProfile().getObjectId();
            updateProfileLastLogin(profileId, mongoSession.getExpiry());
        }

        final var createdSession = getMapper().map(mongoSession, Session.class);

        final SessionCreation sessionCreation = new SessionCreation();
        sessionCreation.setSessionSecret(mongoSessionSecret.getSessionSecret());
        sessionCreation.setSession(createdSession);

        getEventPublisher().accept(Event.builder()
                .argument(createdSession)
                .named(SESSION_CREATED)
                .build());

        return sessionCreation;

    }

    private boolean updateProfileLastLogin(final ObjectId profileId, final Timestamp timestamp) {
        try {

            final var query = getDatastore().find(MongoProfile.class);

            final var result = query
                 .filter(eq("_id", profileId))
                 .update(set("lastLogin", timestamp))
                 .execute(new UpdateOptions().upsert(false));

            if (result.getModifiedCount() == 0) {
                logger.error("Failed to save lastLogin to profile (no record matching id)");
                return false;
            } else {
                return true;
            }

        } catch (MongoCommandException ex) {
            logger.error("Failed to save lastLogin to profile: {}", ex.toString());
            return false;
        }
    }

    @Override
    public void blacklist(final String sessionSecret) {

        final ObjectId mongoUserId;
        final MongoSessionSecret mongoSessionSecret;

        try {
            mongoSessionSecret = new MongoSessionSecret(sessionSecret);
            mongoUserId = mongoSessionSecret.getContextAsObjectId();
        } catch (IllegalArgumentException ex) {
            throw new BadSessionSecretException(ex, "Bad Session Secret");
        }

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final MongoUser mongoUser = getMongoUserDao().getMongoUser(mongoUserId);
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        final Query<MongoSession> query = getDatastore().find(MongoSession.class);

        query.filter(eq("_id", sessionId))
             .filter(eq("user", mongoUser));

        final var existing = query.first();

        if (existing == null) {
            throw new NotFoundException("Session Not Found.");
        }

        final DeleteResult dr = query.delete();

        if (dr.getDeletedCount() == 0) {
            throw new NotFoundException("Session Not Found.");
        } else if (dr.getDeletedCount() > 1) {
            logger.error("Deleted more than one session: {}", dr.getDeletedCount());
        }

        final var deletedSession = getMapper().map(existing, Session.class);

        getEventPublisher().accept(Event.builder()
                .argument(deletedSession)
                .named(SESSION_DELETED)
                .build());

    }

    private void deleteAllSessionsForUser(final String userId) {

        final MongoUser mongoUser = getMongoUserDao().getMongoUser(userId);
        final Query<MongoSession> query = getDatastore().find(MongoSession.class);

        query.filter(eq("user", mongoUser));
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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public Consumer<Event> getEventPublisher() {
        return eventPublisher;
    }

    @Inject
    public void setEventPublisher(Consumer<Event> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

}
