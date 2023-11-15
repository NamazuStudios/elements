package dev.getelements.elements.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.getelements.elements.dao.GoogleSignInSessionDao;
import dev.getelements.elements.dao.IosApplicationConfigurationDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.InvalidPemException;
import dev.getelements.elements.exception.application.ApplicationConfigurationNotFoundException;
import dev.getelements.elements.model.googlesignin.JWKSet;
import dev.getelements.elements.model.googlesignin.TokenResponse;
import dev.getelements.elements.model.application.GoogleSignInConfiguration;
import dev.getelements.elements.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.GoogleSignInSessionCreation;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.NameService;
import dev.getelements.elements.util.PemDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

public class GoogleSignInAuthServiceOperations {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSignInAuthServiceOperations.class);

    private static final String EC_ALGO = "EC";

    private static final String RSA_ALGO = "RSA";

    private static final String BASE_URL = "https://googleid.google.com/auth";

    private static final String KEYS_URI = "/keys";

    private static final String TOKEN_URI = "/token";

    private static final long TOKEN_EXPIRY = MINUTES.toSeconds(30);

    private static final String TOKEN_ISSUER = "https://googleid.google.com";

    private static final String TOKEN_AUDIENCE = "https://googleid.google.com";

    private Client client;

    private NameService nameService;

    private ProfileDao profileDao;

    private GoogleSignInSessionDao googleSignInSessionDao;

    private IosApplicationConfigurationDao iosApplicationConfigurationDao;

    public GoogleSignInSessionCreation createOrUpdateUserWithGoogleSignInTokenAndAuthorizationCode(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final String identityToken,
            final String authorizationCode,
            final Function<DecodedJWT, User> userMapper) {

        final GooglePlayApplicationConfiguration appConfiguration = getApplicationConfiguration(
                applicationNameOrId,
                applicationConfigurationNameOrId
        );

        final GoogleSignInConfiguration googleSignInConfiguration = getGoogleSignInConfiguration(appConfiguration);

        // Attempts to validate the identity, and if it is not valid presumes that the request should be forbidden.
        final DecodedJWT googleIdentityToken = verify(identityToken, googleSignInConfiguration);

        // Maps the user, writing it to the database.
        final User user = userMapper.apply(googleIdentityToken);

        final Profile profile = getProfileDao().createOrRefreshProfile(map(
                user,
                appConfiguration)
        );

        final TokenResponse tokenResponse = fetchRefreshToken(
                authorizationCode,
                googleIdentityToken,
                googleSignInConfiguration);

        final Session session = new Session();

        session.setUser(user);
        session.setProfile(profile);
        session.setApplication(profile.getApplication());

        return getGoogleSignInSessionDao().create(session, tokenResponse);

    }

    private DecodedJWT verify(final String identityToken, final GoogleSignInConfiguration googleSignInConfiguration) {

        final DecodedJWT jwt;

        try {
            jwt = JWT.decode(identityToken);
        } catch (JWTDecodeException ex) {
            throw new InvalidDataException(ex.getMessage(), ex);
        }

        return fetchPublicKeys()
                .map(algorithm -> attemptVerify(jwt, algorithm, googleSignInConfiguration))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(ForbiddenException::new);

    }

    private DecodedJWT attemptVerify(final DecodedJWT jwt,
                                     final Algorithm algorithm,
                                     final GoogleSignInConfiguration googleSignInConfiguration) {
        try {

            final var verifier = JWT
                    .require(algorithm)
                    .withIssuer(TOKEN_ISSUER)
                    .withAudience(googleSignInConfiguration.getClientId())
                    .build();

            return verifier.verify(jwt);

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

        if (jwkSet.getKeys() == null) throw new InternalException("Error communicating with Google servers.");

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
                        final GooglePlayApplicationConfiguration facebookApplicationConfiguration) {
        final Profile profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(getNameService().generateQualifiedName());
        profile.setApplication(facebookApplicationConfiguration.getParent());
        return profile;
    }

    private GooglePlayApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                    final String applicationConfigurationNameOrId) {

        final GooglePlayApplicationConfiguration appConfiguration;

        try {
            appConfiguration = getIosApplicationConfigurationDao().getIosApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId);
        } catch (ApplicationConfigurationNotFoundException ex) {
            throw new InternalException(ex);
        }

        return appConfiguration;

    }

    private GoogleSignInConfiguration getGoogleSignInConfiguration(final GooglePlayApplicationConfiguration configuration) {

        final GoogleSignInConfiguration googleSignInConfiguration = configuration.getGoogleSignInConfiguration();

        if (googleSignInConfiguration == null) {
            final String msg = format("Google Sign-In not configured for %s", configuration.getApplicationId());
            throw new InternalException(msg);
        } else if (googleSignInConfiguration.getClientId() == null) {
            googleSignInConfiguration.setGoogleSignInPrivateKey(configuration.getApplicationId());
        }

        return googleSignInConfiguration;

    }

    private TokenResponse fetchRefreshToken(final String authorizationCode,
                                            final DecodedJWT googleIdentityToken,
                                            final GoogleSignInConfiguration googleSignInConfiguration) {

        final String clientId = googleSignInConfiguration.getClientId();
        final String clientSecret = generateClientSecret(googleSignInConfiguration, googleIdentityToken);

        final Form form = new Form()
                .param(AuthParameter.CLIENT_ID.value, clientId)
                .param(AuthParameter.CLIENT_SECRET.value, clientSecret)
                .param(AuthParameter.GRANT_TYPE.value, GrantType.AUTHORIZATION_CODE.value)
                .param(AuthParameter.CODE.value, authorizationCode.trim());

        final Response response = getClient()
                .target(BASE_URL)
                .path(TOKEN_URI)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.form(form));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            return response.readEntity(TokenResponse.class);
        } else if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            final Object obj = response.readEntity(Object.class);
            logger.error("Caught error from Google servers {} {}", response.getStatus(), obj);
            final String msg = format("Failed to validate Google Sign-In: %s", obj);
            throw new ForbiddenException(msg);
        } else {
            final Object obj = response.readEntity(Object.class);
            logger.error("Caught error from Google servers {} {}", response.getStatus(), obj);
            final String msg = format("Got response from Google %d %s", response.getStatus(), obj.toString());
            throw new InternalException(msg);
        }

    }

    private String generateClientSecret(final GoogleSignInConfiguration googleSignInConfiguration,
                                        final DecodedJWT googleIdentityToken) {

        final long now = MILLISECONDS.toSeconds(currentTimeMillis());

        final ECPrivateKey ecPrivateKey;

        try {

            final PemDecoder<PKCS8EncodedKeySpec> pemDecoder = new PemDecoder<>(
                    googleSignInConfiguration.getGoogleSignInPrivateKey(),
                    PKCS8EncodedKeySpec::new
            );

            ecPrivateKey = (ECPrivateKey) KeyFactory
                    .getInstance(EC_ALGO)
                    .generatePrivate(pemDecoder.getSpec());

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidPemException e) {
            throw new InternalException(e);
        }

        final Algorithm algorithm = Algorithm.ECDSA256(null, ecPrivateKey);

        return JWT.create()
                .withKeyId(googleSignInConfiguration.getKeyId().trim())
                .withClaim(PublicClaims.ISSUER, googleSignInConfiguration.getTeamId().trim())
                .withClaim(PublicClaims.SUBJECT, googleSignInConfiguration.getClientId().trim())
                .withClaim(PublicClaims.ISSUED_AT, now)
                .withClaim(PublicClaims.EXPIRES_AT, now + TOKEN_EXPIRY)
                .withClaim(PublicClaims.AUDIENCE, TOKEN_AUDIENCE)
                .sign(algorithm);

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

    public GoogleSignInSessionDao getGoogleSignInSessionDao() {
        return googleSignInSessionDao;
    }

    @Inject
    public void setGoogleSignInSessionDao(GoogleSignInSessionDao googleSignInSessionDao) {
        this.googleSignInSessionDao = googleSignInSessionDao;
    }

    public IosApplicationConfigurationDao getIosApplicationConfigurationDao() {
        return iosApplicationConfigurationDao;
    }

    @Inject
    public void setIosApplicationConfigurationDao(IosApplicationConfigurationDao iosApplicationConfigurationDao) {
        this.iosApplicationConfigurationDao = iosApplicationConfigurationDao;
    }

    public enum AuthParameter {

        CLIENT_ID("client_id"),
        CLIENT_SECRET("client_secret"),
        CODE("code"),
        GRANT_TYPE("grant_type"),
        REFRESH_TOKEN("refresh_token"),
        REDIRECT_URI("redirect_uri");

        public final String value;

        AuthParameter(final String value) {
            this.value = value;
        }

    }

    public enum GrantType {

        AUTHORIZATION_CODE("authorization_code"),

        REFRESH_TOKEN("refresh_token");

        public final String value;

        GrantType(final String value) {
            this.value = value;
        }

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
