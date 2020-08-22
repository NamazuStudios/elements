package com.namazustudios.socialengine.dao.mongo.applesignin;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.AppleSignInSessionDao;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoAppleSignInSession;
import com.namazustudios.socialengine.dao.mongo.model.MongoSessionSecret;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.applesignin.TokenResponse;
import com.namazustudios.socialengine.model.session.AppleSignInSession;
import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.dozer.Mapper;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Optional;

import static com.namazustudios.socialengine.dao.mongo.model.MongoSession.Type.APPLE_SIGN_IN;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

public class MongoAppleSignInSessionDao implements AppleSignInSessionDao {

    private Mapper mapper;

    private MongoUserDao mongoUserDao;

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Provider<MessageDigest> messageDigestProvider;

    @Override
    public AppleSignInSessionCreation create(final Session session, final TokenResponse tokenResponse) {

        requireNonNull(tokenResponse, "tokenResponse");
        requireNonNull(tokenResponse.getAccessToken(), "tokenResponse.accessToken");
        requireNonNull(tokenResponse.getRefreshToken(), "tokenResponse.refreshToken");

        final MongoAppleSignInSession mongoAppleSignInSession = getMapper().map(session, MongoAppleSignInSession.class);

        // Apple Sign-In created sessions do not expire automatically, they are expired by Apple when a refresh token
        // fails to refresh with Apple's REST APIs. This way we can always recall the session if we need to.

        mongoAppleSignInSession.setExpiry(null);
        mongoAppleSignInSession.setAppleSignInRefreshTime(new Timestamp(currentTimeMillis()));

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
        creation.setUserAccessToken(tokenResponse.getAccessToken());

        return creation;

    }

    public void validate(final Session session) {
        getValidationHelper().validateModel(session);
    }

    @Override
    public Optional<AppleSignInSession> findSession(final String sessionSecret) {
        return Optional.empty();
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
