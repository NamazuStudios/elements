package dev.getelements.elements.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.auth.JWK;
import dev.getelements.elements.sdk.model.auth.JWKSet;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.auth.OidcClaim;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.name.NameService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import kotlin.jvm.functions.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class OidcAuthServiceOperations {

    private static final Logger logger = LoggerFactory.getLogger(OidcAuthServiceOperations.class);

    private static final String RSA_ALGO = "RSA";

    private Client client;

    private NameService nameService;

    private ProfileDao profileDao;

    private SessionDao SessionDao;

    private ApplicationDao applicationDao;

    private OidcAuthSchemeDao oidcAuthSchemeDao;

    private long sessionTimeoutSeconds;


    public SessionCreation createOrUpdateUserWithToken(
            final OidcSessionRequest oidcSessionRequest,
            final Function2<DecodedJWT, OidcAuthScheme, User> userMapper) {

        final DecodedJWT decodedJWT;
        final var identityToken = oidcSessionRequest.getJwt();

        try {
            decodedJWT = JWT.decode(identityToken);
        } catch (JWTDecodeException ex) {
            throw new InvalidDataException(ex.getMessage(), ex);
        }

        final var schemeId = decodedJWT.getClaim(OidcAuthServiceOperations.Claim.SCHEME.value).asString();
        final var schemeSearch = getOidcAuthSchemeDao().findAuthScheme(schemeId);

        if(schemeSearch.isEmpty()) {
            throw new ForbiddenException("No scheme with issuer " + schemeId + " was found");
        }

        final var scheme = schemeSearch.get();

        // Attempts to validate the identity, and if it is not valid presumes that the request should be forbidden.
        verify(decodedJWT, scheme);

        // Maps the user, writing it to the database.
        final User user = userMapper.invoke(decodedJWT, scheme);
        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        final var session = new Session();
        final var applicationId = decodedJWT.getClaim(OidcAuthServiceOperations.Claim.APPLICATION_ID.value).asString();

        session.setUser(user);
        session.setExpiry(expiry);

        if(applicationId != null) {

            final var applicationOptional = getApplicationDao().findActiveApplication(applicationId);

            if(applicationOptional.isPresent()) {
                final var application = applicationOptional.get();
                final var profile = getProfileDao().createOrRefreshProfile(
                        map(user, application)
                );

                session.setProfile(profile);
                session.setApplication(application);
            }
        }

        return getSessionDao().create(session);
    }

    private void verify(final DecodedJWT jwt, final OidcAuthScheme scheme) {

        final var kid = jwt.getHeaderClaim(OidcClaim.KID.getValue()).asString();

        final var jwk = scheme.getKeys()
                .stream()
                .filter(k -> Objects.equals(k.getKid(), kid))
                .findFirst()
                .orElse(null);

        if(jwk != null) {
            final var algorithm = getAlgorithmFromJWK(jwk);
            attemptVerify(jwt, algorithm);
            return;
        }

        //If we don't have a matching JWK for the provided KID, attempt to fetch
        if(scheme.getKeysUrl() != null) {
            fetchPublicKeys(kid, scheme)
                .map(algorithm -> attemptVerify(jwt, algorithm))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("No matching JWK for the provided key id"));
        }

        throw new ForbiddenException("No matching JWK for the provided key id");
    }

    private DecodedJWT attemptVerify(final DecodedJWT jwt,
                                     final Algorithm algorithm) {
        try {

            //Check the signature
            algorithm.verify(jwt);

            //Check the expiry
            final var exp = jwt.getClaim("exp").asLong();
            final var now = currentTimeMillis() / 1000;

            if(exp < now) {
                throw new ForbiddenException("Token has expired");
            }

            return jwt;

        } catch (JWTVerificationException ex) {
            logger.trace("Key verification failed for {}", algorithm, ex);
            return null;
        }

    }

    private Stream<Algorithm> fetchPublicKeys(String kid, OidcAuthScheme scheme) {

        final JWKSet jwkSet = getClient()
                .target(scheme.getKeysUrl())
                .request(scheme.getMediaType())
                .get(JWKSet.class);

        if (jwkSet.getKeys() == null) throw new InternalException("Error fetching JWKs at " + scheme.getKeysUrl());

        //Update scheme with new keys
        scheme.setKeys(jwkSet.getKeys());
        getOidcAuthSchemeDao().updateAuthScheme(scheme);

        return jwkSet.getKeys()
                .stream()
                .map(this::getAlgorithmFromJWK);
    }

    private Algorithm getAlgorithmFromJWK(JWK k) {

        final BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(k.getN()));
        final BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(k.getE()));

        final RSAPublicKey publicKey;

        try {
            final RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
            publicKey = (RSAPublicKey) KeyFactory.getInstance(RSA_ALGO).generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new InternalException(ex);
        }

        //TODO: Get algorithm type from JWK or JWT header
        return Algorithm.RSA256(publicKey, null);
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
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public SessionDao getSessionDao() {
        return SessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao SessionDao) {
        this.SessionDao = SessionDao;
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

    public OidcAuthSchemeDao getOidcAuthSchemeDao() {
        return oidcAuthSchemeDao;
    }

    @Inject
    public void setOidcAuthSchemeDao(OidcAuthSchemeDao oidcAuthSchemeDao) {
        this.oidcAuthSchemeDao = oidcAuthSchemeDao;
    }

    public enum Claim {

        EMAIL("email"),

        USER_ID(OidcClaim.SUB.getValue()),

        SCHEME(OidcClaim.ISS.getValue()),
        APPLICATION_ID(OidcClaim.AUD.getValue());

        public final String value;

        Claim(final String value) {
            this.value = value;
        }

    }

}
