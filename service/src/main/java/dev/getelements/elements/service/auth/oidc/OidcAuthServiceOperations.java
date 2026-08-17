package dev.getelements.elements.service.auth.oidc;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.service.Constants.OIDC_JWKS_REFRESH_SECONDS;
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

    private long jwksRefreshIntervalSeconds;


    public SessionCreation createOrUpdateUserWithToken(
            final OidcSessionRequest oidcSessionRequest,
            final BiFunction<DecodedJWT, OidcAuthScheme, User> userMapper) {

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
        // No expected audience/nonce here: this is the direct-id_token path, which has no attempt-bound nonce,
        // and today's schemes carry no client id to check the audience against.
        verify(decodedJWT, scheme, null, null);

        return buildSession(decodedJWT, scheme, userMapper, oidcSessionRequest.getApplicationNameOrId());
    }

    /**
     * Decodes and validates a possessed id_token against the given scheme, additionally checking the audience
     * (if {@code expectedAudience} is non-null) and nonce (if {@code expectedNonce} is non-null) claims. Shared by
     * the direct id_token path ({@link #createOrUpdateUserWithToken}, which passes both as {@code null}) and the
     * browser-redirect callback path, which passes the provider's client id and the attempt's bound nonce.
     *
     * @param idToken the id_token to decode and validate
     * @param scheme the scheme to validate against
     * @param expectedAudience the expected 'aud' claim value, or {@code null} to skip the check
     * @param expectedNonce the expected 'nonce' claim value, or {@code null} to skip the check
     * @return the decoded, validated JWT
     */
    public DecodedJWT decodeAndVerify(final String idToken,
                                       final OidcAuthScheme scheme,
                                       final String expectedAudience,
                                       final String expectedNonce) {

        final DecodedJWT decodedJWT;

        try {
            decodedJWT = JWT.decode(idToken);
        } catch (JWTDecodeException ex) {
            throw new InvalidDataException(ex.getMessage(), ex);
        }

        verify(decodedJWT, scheme, expectedAudience, expectedNonce);
        return decodedJWT;

    }

    /**
     * Maps and creates/updates the user and session for an already-validated JWT. Callers that have already
     * validated the token (e.g. via {@link #decodeAndVerify}) use this directly rather than re-validating through
     * {@link #createOrUpdateUserWithToken}.
     *
     * @param decodedJWT the already-validated, decoded JWT
     * @param scheme the scheme the token was validated against
     * @param userMapper resolves/creates the {@link User} for the token
     * @return the created session
     */
    public SessionCreation createOrUpdateUserWithVerifiedToken(
            final DecodedJWT decodedJWT,
            final OidcAuthScheme scheme,
            final BiFunction<DecodedJWT, OidcAuthScheme, User> userMapper) {
        return buildSession(decodedJWT, scheme, userMapper, null);
    }

    private SessionCreation buildSession(final DecodedJWT decodedJWT,
                                          final OidcAuthScheme scheme,
                                          final BiFunction<DecodedJWT, OidcAuthScheme, User> userMapper,
                                          final String requestedApplicationNameOrId) {

        // Maps the user, writing it to the database.
        final User user = userMapper.apply(decodedJWT, scheme);
        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();
        final var session = new Session();

        // The request-supplied application takes precedence over the JWT's own aud claim. It lets a caller
        // explicitly opt into a gated auto-create (subject to autoCreateProfile/maxProfiles); the aud claim
        // alone preserves the legacy, deliberately ungated get-or-create behavior.
        final var applicationId = requestedApplicationNameOrId != null
                ? requestedApplicationNameOrId
                : decodedJWT.getClaim(OidcAuthServiceOperations.Claim.APPLICATION_ID.value).asString();

        session.setUser(user);
        session.setExpiry(expiry);

        if(applicationId != null) {

            final var applicationOptional = getApplicationDao().findActiveApplication(applicationId);

            if(applicationOptional.isPresent()) {

                final var application = applicationOptional.get();
                final var existingProfile = getProfileDao().findPrimaryProfile(user.getId(), application.getId());

                final Profile profile = existingProfile.isPresent()
                        ? existingProfile.get()
                        : requestedApplicationNameOrId != null
                                ? autoCreatePrimaryProfileIfConfigured(user, application)
                                : getProfileDao().createOrRefreshProfile(map(user, application));

                if (profile != null) {
                    session.setProfile(profile);
                    session.setApplication(application);
                }

            }
        }

        return getSessionDao().create(session);
    }

    private Profile autoCreatePrimaryProfileIfConfigured(final User user, final Application application) {

        final var maxProfiles = application.getMaxProfiles();

        if (!Boolean.TRUE.equals(application.getAutoCreateProfile()) || maxProfiles == null || maxProfiles < 1) {
            return null;
        }

        return getProfileDao().createSlottedProfile(map(user, application), Map.of());

    }

    private void verify(final DecodedJWT jwt,
                         final OidcAuthScheme scheme,
                         final String expectedAudience,
                         final String expectedNonce) {

        if (scheme.getIssuer() != null && !scheme.getIssuer().equals(jwt.getIssuer())) {
            throw new ForbiddenException("Issuer mismatch");
        }

        if (expectedAudience != null) {
            final var audience = jwt.getAudience();
            if (audience == null || !audience.contains(expectedAudience)) {
                throw new ForbiddenException("Audience mismatch");
            }
        }

        if (expectedNonce != null) {
            final var nonce = jwt.getClaim("nonce").asString();
            if (!expectedNonce.equals(nonce)) {
                throw new ForbiddenException("Nonce mismatch");
            }
        }

        final var kid = jwt.getHeaderClaim(OidcClaim.KID.getValue()).asString();

        final var jwk = scheme.getKeys()
                .stream()
                .filter(k -> Objects.equals(k.getKid(), kid))
                .findFirst()
                .orElse(null);

        //Staleness only forces a refresh when there's a keysUrl to refresh from; a scheme with no keysUrl has
        //no other source of truth, so its keys are always trusted as-is.
        if(jwk != null && !(scheme.getKeysUrl() != null && isKeysStale(scheme))) {
            final var algorithm = getAlgorithmFromJWK(jwk);
            attemptVerify(jwt, algorithm);
            return;
        }

        //If we don't have a matching, fresh JWK for the provided KID, attempt to fetch
        if(scheme.getKeysUrl() != null) {
            fetchPublicKeys(kid, scheme)
                .map(algorithm -> attemptVerify(jwt, algorithm))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("No matching JWK for the provided key id"));
            return;
        }

        throw new ForbiddenException("No matching JWK for the provided key id");
    }

    private boolean isKeysStale(final OidcAuthScheme scheme) {
        final var keysFetchedAt = scheme.getKeysFetchedAt();
        if (keysFetchedAt == null) return true;
        final var now = currentTimeMillis() / 1000;
        return now - keysFetchedAt > getJwksRefreshIntervalSeconds();
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
        scheme.setKeysFetchedAt(currentTimeMillis() / 1000);
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

    public long getJwksRefreshIntervalSeconds() {
        return jwksRefreshIntervalSeconds;
    }

    @Inject
    public void setJwksRefreshIntervalSeconds(@Named(OIDC_JWKS_REFRESH_SECONDS) long jwksRefreshIntervalSeconds) {
        this.jwksRefreshIntervalSeconds = jwksRefreshIntervalSeconds;
    }

    /**
     * The standard OIDC {@code profile} scope claim names (OpenID Connect Core 1.0 §5.1), excluding {@code sub}/
     * {@code email}/{@code email_verified} (handled separately, with dedicated user-uid linking) and
     * token/session metadata such as {@code aud}/{@code iss}/{@code exp}/{@code iat}/{@code nonce} (not profile
     * data, and mostly change on every login).
     */
    private static final List<String> PROFILE_CLAIM_NAMES = List.of(
            "name", "given_name", "family_name", "middle_name", "nickname", "preferred_username",
            "profile", "picture", "website", "gender", "birthdate", "zoneinfo", "locale", "updated_at"
    );

    /**
     * Reads a claim as a string, tolerating a bare {@code null} from {@link DecodedJWT#getClaim(String)} — a real
     * {@code DecodedJWT} never returns null there (a missing claim comes back as a {@code NullClaim} whose
     * {@code asString()} is null), but a mocked one used in tests can return a bare null for an unstubbed claim
     * name. All claim reads in this package should go through this rather than calling {@code getClaim(...)
     * .asString()} directly.
     *
     * @param jwt the decoded id_token
     * @param claimName the claim name
     * @return the claim's string value, or {@code null} if absent
     */
    public static String claimAsString(final DecodedJWT jwt, final String claimName) {
        final var claim = jwt.getClaim(claimName);
        return claim == null ? null : claim.asString();
    }

    /**
     * Extracts whichever standard OIDC profile-scope claims are actually present in the given token, keyed by
     * their raw claim name. Used to snapshot a linked scheme's reported profile data onto {@link User#getLinkedAccountProfiles()}.
     *
     * @param jwt the decoded id_token
     * @return a map of present profile claim names to their string values; empty if none are present
     */
    public static Map<String, String> extractProfileClaims(final DecodedJWT jwt) {

        final var claims = new HashMap<String, String>();

        for (final var claimName : PROFILE_CLAIM_NAMES) {
            final var value = claimAsString(jwt, claimName);
            if (value != null && !value.isEmpty()) {
                claims.put(claimName, value);
            }
        }

        return claims;

    }

    public enum Claim {

        EMAIL("email"),

        PREFERRED_USERNAME("preferred_username"),

        GIVEN_NAME("given_name"),

        FAMILY_NAME("family_name"),

        USER_ID(OidcClaim.SUB.getValue()),

        SCHEME(OidcClaim.ISS.getValue()),
        APPLICATION_ID(OidcClaim.AUD.getValue());

        public final String value;

        Claim(final String value) {
            this.value = value;
        }

    }

}
