package dev.getelements.elements.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.dao.AppleSignInSessionDao;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.applesignin.JWKSet;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.AppleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.NameService;
import dev.getelements.elements.service.Unscoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.getelements.elements.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.*;

public class AppleSignInAuthServiceOperations {

    private static final Logger logger = LoggerFactory.getLogger(AppleSignInAuthServiceOperations.class);

    private static final String RSA_ALGO = "RSA";

    private static final String BASE_URL = "https://appleid.apple.com/auth";

    private static final String KEYS_URI = "/keys";

    private Client client;

    private NameService nameService;

    private ProfileDao profileDao;

    private AppleSignInSessionDao appleSignInSessionDao;

    private ApplicationDao applicationDao;

    private long sessionTimeoutSeconds;


    public AppleSignInSessionCreation createOrUpdateUserWithAppleSignInToken(
            final String applicationNameOrId,
            final String identityToken,
            final Function<DecodedJWT, User> userMapper) {

        // Attempts to validate the identity, and if it is not valid presumes that the request should be forbidden.
        final DecodedJWT appleIdentityToken = verify(identityToken);

        // Maps the user, writing it to the database.
        final User user = userMapper.apply(appleIdentityToken);
        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        final var session = new Session();

        session.setUser(user);
        session.setExpiry(expiry);

        if(applicationNameOrId != null) {

            final var application = getApplicationDao().getActiveApplication(applicationNameOrId);
            final var profile = getProfileDao().createOrRefreshProfile(
                    map(user, application)
            );

            session.setProfile(profile);
            session.setApplication(application);
        }

        return getAppleSignInSessionDao().create(session);
    }

    private DecodedJWT verify(final String identityToken) {

        final DecodedJWT jwt;

        try {
            jwt = JWT.decode(identityToken);
        } catch (JWTDecodeException ex) {
            throw new InvalidDataException(ex.getMessage(), ex);
        }

        return fetchPublicKeys()
            .map(algorithm -> attemptVerify(jwt, algorithm))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(ForbiddenException::new);

    }

    private DecodedJWT attemptVerify(final DecodedJWT jwt,
                                     final Algorithm algorithm) {
        try {

            //Check the signature
            algorithm.verify(jwt);

            //Check the expiry
            final var exp = jwt.getClaim("exp").asLong();
            final var now = currentTimeMillis() / 1000;

            if(exp > now) {
                throw new ForbiddenException("Token has expired");
            }

            return jwt;

        } catch (JWTVerificationException ex) {
            logger.trace("Key verification failed for {}", algorithm, ex);
            return null;
        }


    }

    private Stream<Algorithm> fetchPublicKeys() {

        final JWKSet jwkSet = getClient()
            .target(BASE_URL)
            .path(KEYS_URI)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(JWKSet.class);

        if (jwkSet.getKeys() == null) throw new InternalException("Error communicating with Apple servers.");

        return jwkSet.getKeys()
            .stream()
            .map(k -> {

                final BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(k.getN()));
                final BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(k.getE()));

                final RSAPublicKey publicKey;

                try {
                    final RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
                    publicKey = (RSAPublicKey) KeyFactory.getInstance(RSA_ALGO).generatePublic(keySpec);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    throw new InternalException(ex);
                }

                return Algorithm.RSA256(publicKey, null);

            }).filter(algo -> algo != null);

    }

    private Profile map(final User user,
                        final Application application) {

        final var profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(getNameService().generateQualifiedName());
        profile.setApplication(application);

        return profile;
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(@Unscoped NameService nameService) {
        this.nameService = nameService;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public AppleSignInSessionDao getAppleSignInSessionDao() {
        return appleSignInSessionDao;
    }

    @Inject
    public void setAppleSignInSessionDao(AppleSignInSessionDao appleSignInSessionDao) {
        this.appleSignInSessionDao = appleSignInSessionDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

    public enum Claim {

        EMAIL("email"),

        USER_ID(PublicClaims.SUBJECT);

        public final String value;

        Claim(final String value) {
            this.value = value;
        }

    }

}
