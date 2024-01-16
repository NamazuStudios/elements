package dev.getelements.elements.dao.mongo.applesignin;

import dev.getelements.elements.Constants;
import dev.getelements.elements.dao.AppleSignInSessionDao;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.model.MongoAppleSignInSession;
import dev.getelements.elements.dao.mongo.model.MongoSession;
import dev.getelements.elements.dao.mongo.model.MongoSessionSecret;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.exception.security.BadSessionSecretException;
import dev.getelements.elements.model.applesignin.TokenResponse;
import dev.getelements.elements.model.session.AppleSignInSession;
import dev.getelements.elements.model.session.AppleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.query.filters.Filters;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Optional;

import static dev.getelements.elements.dao.mongo.model.MongoSession.Type.APPLE_SIGN_IN;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

public class MongoAppleSignInSessionDao implements AppleSignInSessionDao {

    private Mapper mapper;

    private MongoUserDao mongoUserDao;

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Provider<MessageDigest> messageDigestProvider;

    @Override
    public AppleSignInSessionCreation create(final Session session) {

        requireNonNull(session, "Session");

        final MongoAppleSignInSession mongoAppleSignInSession = getMapper().map(session, MongoAppleSignInSession.class);
        mongoAppleSignInSession.setExpiry(new Timestamp(session.getExpiry()));

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(session.getUser());
        final MongoSessionSecret mongoSessionSecret = new MongoSessionSecret(mongoUser.getObjectId());

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        mongoAppleSignInSession.setType(APPLE_SIGN_IN);
        mongoAppleSignInSession.setSessionId(sessionId);

        getDatastore().save(mongoAppleSignInSession);

        final AppleSignInSessionCreation creation = new AppleSignInSessionCreation();

        creation.setSessionSecret(mongoSessionSecret.getSessionSecret());
        creation.setSession(getMapper().map(mongoAppleSignInSession, Session.class));

        return creation;

    }

    @Override
    public Optional<AppleSignInSession> findSession(String sessionSecret) {

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

        final Query<MongoAppleSignInSession> query = getDatastore().find(MongoAppleSignInSession.class);

        query.filter(Filters.and(
                Filters.eq("_id", sessionId),
                Filters.eq("type", APPLE_SIGN_IN)
        ));

        final MongoSession mongoSession = query.first();

        if (mongoSession == null) {
            return Optional.empty();
        } else {
            final AppleSignInSession appleSignInSession = getMapper().map(mongoSession, AppleSignInSession.class);
            return Optional.ofNullable(appleSignInSession);
        }

    }

    public void validate(final Session session) {
        getValidationHelper().validateModel(session);
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
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

    public Provider<MessageDigest> getMessageDigestProvider() {
        return messageDigestProvider;
    }

    @Inject
    public void setMessageDigestProvider(@Named(Constants.PASSWORD_DIGEST) Provider<MessageDigest> messageDigestProvider) {
        this.messageDigestProvider = messageDigestProvider;
    }

}
