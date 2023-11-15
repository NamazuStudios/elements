package dev.getelements.elements.dao.mongo.googlesignin;

import dev.getelements.elements.Constants;
import dev.getelements.elements.dao.GoogleSignInSessionDao;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.model.MongoGoogleSignInSession;
import dev.getelements.elements.dao.mongo.model.MongoSession;
import dev.getelements.elements.dao.mongo.model.MongoSessionSecret;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.exception.security.BadSessionSecretException;
import dev.getelements.elements.model.googlesignin.TokenResponse;
import dev.getelements.elements.model.session.GoogleSignInSession;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Optional;

import static dev.getelements.elements.dao.mongo.model.MongoSession.Type.GOOGLE_SIGN_IN;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

public class MongoGoogleSignInSessionDao implements GoogleSignInSessionDao {

    private Mapper mapper;

    private MongoUserDao mongoUserDao;

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Provider<MessageDigest> messageDigestProvider;

    @Override
    public GoogleSignInSessionCreation create(final Session session, final TokenResponse tokenResponse) {

        requireNonNull(tokenResponse, "tokenResponse");
        requireNonNull(tokenResponse.getAccessToken(), "tokenResponse.accessToken");
        requireNonNull(tokenResponse.getRefreshToken(), "tokenResponse.refreshToken");

        final MongoGoogleSignInSession mongoGoogleSignInSession = getMapper().map(session, MongoGoogleSignInSession.class);

        // Google Sign-In created sessions do not expire automatically, they are expired by Google when a refresh token
        // fails to refresh with Google's REST APIs. This way we can always recall the session if we need to.

        mongoGoogleSignInSession.setExpiry(null);
        mongoGoogleSignInSession.setGoogleSignInRefreshTime(new Timestamp(currentTimeMillis()));

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(session.getUser());
        final MongoSessionSecret mongoSessionSecret = new MongoSessionSecret(mongoUser.getObjectId());

        final MessageDigest messageDigest = getMessageDigestProvider().get();
        final String sessionId = mongoSessionSecret.getSecretDigestEncoded(messageDigest, mongoUser.getPasswordHash());

        mongoGoogleSignInSession.setType(GOOGLE_SIGN_IN);
        mongoGoogleSignInSession.setSessionId(sessionId);

        getDatastore().save(mongoGoogleSignInSession);

        final GoogleSignInSessionCreation creation = new GoogleSignInSessionCreation();

        creation.setSessionSecret(mongoSessionSecret.getSessionSecret());
        creation.setSession(getMapper().map(mongoGoogleSignInSession, Session.class));
        creation.setUserAccessToken(tokenResponse.getAccessToken());

        return creation;

    }

    @Override
    public Optional<GoogleSignInSession> findSession(String sessionSecret) {

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

        final Query<MongoGoogleSignInSession> query = getDatastore().find(MongoGoogleSignInSession.class);

        query.filter(Filters.and(
                Filters.eq("_id", sessionId),
                Filters.eq("type", GOOGLE_SIGN_IN)
        ));

        final MongoSession mongoSession = query.first();

        if (mongoSession == null) {
            return Optional.empty();
        } else {
            final GoogleSignInSession googleSignInSession = getMapper().map(mongoSession, GoogleSignInSession.class);
            return Optional.ofNullable(googleSignInSession);
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
