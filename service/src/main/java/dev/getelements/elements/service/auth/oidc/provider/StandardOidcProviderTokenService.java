package dev.getelements.elements.service.auth.oidc.provider;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.auth.OidcRefreshToken;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenResponse;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.Constants;
import dev.getelements.elements.sdk.service.auth.OidcProviderTokenService;
import dev.getelements.elements.sdk.service.auth.SessionTokenIssuer;
import dev.getelements.elements.sdk.service.auth.SessionTokenVerifier;
import dev.getelements.elements.service.auth.StandardSessionTokenKeySupport;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.CLAIM_AZP;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.CODE_CHALLENGE_METHOD_S256;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_CLIENT;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_GRANT;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_REQUEST;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_SCOPE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_TARGET;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_UNAUTHORIZED_CLIENT;
import static dev.getelements.elements.sdk.service.Constants.OIDC_ID_TOKEN_TTL_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.OIDC_MACHINE_TOKEN_TTL_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.OIDC_PROVIDER_ISSUER;
import static dev.getelements.elements.sdk.service.Constants.OIDC_REFRESH_GRANT_TTL_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;

public class StandardOidcProviderTokenService implements OidcProviderTokenService {

    private static final Logger logger = LoggerFactory.getLogger(StandardOidcProviderTokenService.class);

    private static final int REFRESH_TOKEN_BYTES = 32;

    private static final String ID_TOKEN_ALGORITHM = "RS256";

    private OidcProviderDao oidcProviderDao;

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private SessionTokenIssuer sessionTokenIssuer;

    private SessionTokenVerifier sessionTokenVerifier;

    private StandardSessionTokenKeySupport sessionTokenKeySupport;

    private OidcProviderRateLimiter rateLimiter;

    @Named(OIDC_PROVIDER_ISSUER)
    @Inject
    private String issuer;

    @Named(OIDC_ID_TOKEN_TTL_SECONDS)
    @Inject
    private long idTokenTtlSeconds;

    @Named(OIDC_REFRESH_GRANT_TTL_SECONDS)
    @Inject
    private long refreshGrantTtlSeconds;

    @Named(OIDC_MACHINE_TOKEN_TTL_SECONDS)
    @Inject
    private long machineTokenTtlSeconds;

    @Named(SESSION_TIMEOUT_SECONDS)
    @Inject
    private long sessionTimeoutSeconds;

    @Override
    public OidcProviderTokenResponse exchangeCode(final OidcProviderTokenRequest request) {

        getRateLimiter().checkTokenEndpoint(request.getClientId());

        final var registration = authenticateClient(request, OidcClientRegistration.GrantType.AUTHORIZATION_CODE);

        if (request.getCode() == null || request.getRedirectUri() == null || request.getCodeVerifier() == null) {
            throw new OAuth2Exception(ERROR_INVALID_REQUEST, "code, redirect_uri, and code_verifier are required.");
        }

        final var code = getOidcProviderDao().redeemAuthorizationCode(request.getCode())
                .orElseThrow(() -> new OAuth2Exception(ERROR_INVALID_GRANT, "Invalid authorization code."));

        validateCode(request, registration, code);

        final var user = getUserDao().getUser(code.getUserId());

        if (user == null) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "User for authorization code no longer exists.");
        }

        final var sessionCreation = issueSession(user, code.getApplicationId());
        final var sessionId = requireSessionId(sessionCreation);

        final var now = currentTimeMillis();
        final var grantExpiry = new Timestamp(now + getRefreshGrantTtlSeconds() * 1000);

        final var grant = new OidcProviderGrant();
        grant.setClientId(registration.getClientId());
        grant.setUserId(user.getId());
        grant.setApplicationId(code.getApplicationId());
        grant.setScope(code.getScope());
        grant.setSessionId(sessionId);
        grant.setExpiry(grantExpiry);

        final var createdGrant = getOidcProviderDao().createGrant(grant);

        final var response = new OidcProviderTokenResponse();
        response.setAccessToken(sessionCreation.getSessionSecret());
        response.setExpiresIn(Math.max(1, (sessionCreation.getSession().getExpiry() - now) / 1000));
        response.setScope(code.getScope());
        response.setIdToken(signIdToken(registration.getClientId(), user, code.getNonce(), code.getAuthTime()));

        if (registration.getGrantTypes().contains(OidcClientRegistration.GrantType.REFRESH_TOKEN)) {
            response.setRefreshToken(issueRefreshToken(createdGrant, registration));
        }

        logger.info("Issued tokens to client {} for user {} (scope {}).",
                registration.getClientId(), user.getId(), code.getScope());

        return response;

    }

    @Override
    public OidcProviderTokenResponse refresh(final OidcProviderTokenRequest request) {

        getRateLimiter().checkTokenEndpoint(request.getClientId());

        final var registration = authenticateClient(request, OidcClientRegistration.GrantType.REFRESH_TOKEN);

        if (request.getRefreshToken() == null) {
            throw new OAuth2Exception(ERROR_INVALID_REQUEST, "refresh_token is required.");
        }

        // Consume first, atomically: a replayed refresh token fails closed regardless of what happens after.
        final var consumed = getOidcProviderDao().consumeRefreshToken(sha256Hex(request.getRefreshToken()))
                .orElseThrow(() -> new OAuth2Exception(ERROR_INVALID_GRANT, "Invalid refresh token."));

        if (!consumed.getClientId().equals(registration.getClientId())) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "Refresh token was not issued to this client.");
        }

        final var grant = getOidcProviderDao().getGrant(consumed.getGrantId())
                .orElseThrow(() -> new OAuth2Exception(ERROR_INVALID_GRANT, "Grant for refresh token has expired."));

        if (!grant.getClientId().equals(registration.getClientId())) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "Refresh token was not issued to this client.");
        }

        final var user = getUserDao().getUser(grant.getUserId());

        if (user == null) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "User for grant no longer exists.");
        }

        final var oldSessionId = grant.getSessionId();
        final var sessionCreation = issueSession(user, grant.getApplicationId());
        final var newSessionId = requireSessionId(sessionCreation);

        if (getOidcProviderDao().rotateGrantSession(grant.getId(), oldSessionId, newSessionId).isEmpty()) {

            // A concurrent refresh rotated the grant first; this exchange's freshly issued session must not
            // remain live. Delete it (and any grant row is untouched) and fail.

            getSessionDao().deleteSessionBySessionId(newSessionId);
            logger.warn("Lost refresh rotation race for grant {}.", grant.getId());
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "Refresh token already used.");

        }

        getSessionDao().deleteSessionBySessionId(oldSessionId);

        final var now = currentTimeMillis();

        final var response = new OidcProviderTokenResponse();
        response.setAccessToken(sessionCreation.getSessionSecret());
        response.setExpiresIn(Math.max(1, (sessionCreation.getSession().getExpiry() - now) / 1000));
        response.setScope(grant.getScope());

        // Per OIDC Core section 12.2, a refreshed id_token MUST NOT carry a nonce. The original auth_time is
        // not re-derivable here in 135a; auth_time is therefore present only on exchange-issued id_tokens.

        response.setIdToken(signIdToken(registration.getClientId(), user, null, 0));

        response.setRefreshToken(issueRefreshToken(grant, registration));

        logger.info("Rotated tokens for client {} user {} under grant {}.",
                registration.getClientId(), user.getId(), grant.getId());

        return response;

    }

    @Override
    public OidcProviderTokenResponse clientCredentials(final OidcProviderTokenRequest request) {

        getRateLimiter().checkTokenEndpoint(request.getClientId());

        final var registration = authenticateClient(request, OidcClientRegistration.GrantType.CLIENT_CREDENTIALS);

        final var now = currentTimeMillis();
        final var audience = resolveAudience(request.getAudience(), registration);
        final var scope = resolveMachineScope(request.getScope(), registration);

        final var claims = new JWTClaimsSet.Builder()
                .subject(registration.getClientId())
                .issuer(getIssuer())
                .audience(audience)
                .claim(CLAIM_AZP, registration.getClientId())
                .issueTime(new Date(now))
                .expirationTime(new Date(now + getMachineTokenTtlSeconds() * 1000))
                .jwtID(UUID.randomUUID().toString());

        if (scope != null) {
            claims.claim("scope", scope);
        }

        final var response = new OidcProviderTokenResponse();
        response.setAccessToken(signToken(claims.build()));
        response.setExpiresIn(Math.max(1, getMachineTokenTtlSeconds()));
        response.setScope(scope);

        logger.info("Issued client_credentials token to client {} (audience {}, scope {}).",
                registration.getClientId(), audience, scope);

        return response;

    }

    /**
     * Resolves the audience for a machine token. A requested audience must be permitted: in the client's
     * registered audience allow-list, or the client's own id when none is registered. When omitted, the sole
     * registered audience is used; with several registered (or none registered) the default is the client's
     * application id, or its client id when no application is linked.
     */
    private String resolveAudience(final String requested, final OidcClientRegistration registration) {

        final var allowed = registration.getAudiences();

        if (requested != null && !requested.isBlank()) {

            final var candidate = requested.trim();
            final var permitted = allowed == null || allowed.isEmpty()
                    ? Set.of(registration.getClientId())
                    : allowed;

            if (!permitted.contains(candidate)) {
                throw new OAuth2Exception(ERROR_INVALID_TARGET,
                        "Requested audience is not permitted for this client.");
            }

            return candidate;

        }

        // Never guess: silently issuing aud=<client id> to a proxy expecting a registered audience would
        // hand the operator a token that verifies but is useless — an explicit error is easier to debug.
        if (allowed != null && !allowed.isEmpty()) {
            if (allowed.size() == 1) {
                return allowed.iterator().next();
            }
            throw new OAuth2Exception(ERROR_INVALID_TARGET,
                    "audience is required when the client's registration lists multiple audiences.");
        }

        return registration.getApplicationId() != null
                ? registration.getApplicationId()
                : registration.getClientId();

    }

    /**
     * Resolves the scope for a machine token: a requested scope must be a subset of the client's registered
     * scopes; when omitted, the client's full registered scope set is granted.
     */
    private String resolveMachineScope(final String requested, final OidcClientRegistration registration) {

        if (requested == null || requested.isBlank()) {
            return registration.getScopes() == null || registration.getScopes().isEmpty()
                    ? null
                    : registration.getScopes().stream().sorted().collect(java.util.stream.Collectors.joining(" "));
        }

        final var requestedScopes = Set.of(requested.trim().split("\\s+"));

        if (registration.getScopes() == null || !registration.getScopes().containsAll(requestedScopes)) {
            throw new OAuth2Exception(ERROR_INVALID_SCOPE, "Requested scope exceeds the client's allowed scopes.");
        }

        // Sorted so the issued scope string is stable regardless of the registration set's iteration order.
        return requestedScopes.stream().sorted().collect(java.util.stream.Collectors.joining(" "));

    }

    @Override
    public void endSession(final String idTokenHint) {

        if (idTokenHint == null) {
            throw new OAuth2Exception(ERROR_INVALID_REQUEST, "id_token_hint is required.");
        }

        final var subAndAzp = verifyIdTokenHint(idTokenHint);

        for (final var grant : getOidcProviderDao().getGrantsForUserAndClient(subAndAzp[0], subAndAzp[1])) {

            getOidcProviderDao().revokeRefreshTokensForGrant(grant.getId());
            getOidcProviderDao().deleteGrantBySessionId(grant.getSessionId());
            getSessionDao().deleteSessionBySessionId(grant.getSessionId());

            logger.info("Ended grant {} for user {} on client {}.",
                    grant.getId(), subAndAzp[0], subAndAzp[1]);

        }

    }

    /**
     * Verifies the id_token_hint's signature against this deployment's signing keys and returns the
     * {@code sub} (user id) and {@code azp} (client id) claims.
     */
    private String[] verifyIdTokenHint(final String idTokenHint) {

        try {

            final var signedJwt = SignedJWT.parse(idTokenHint);

            final var kid = signedJwt.getHeader().getKeyID();
            if (kid == null) {
                throw new OAuth2Exception(ERROR_INVALID_REQUEST, "id_token_hint carries no kid.");
            }

            final var publicKey = getSessionTokenKeySupport().getPublicKey(kid);

            if (!signedJwt.verify(new RSASSAVerifier(publicKey))) {
                throw new OAuth2Exception(ERROR_INVALID_REQUEST, "id_token_hint signature is invalid.");
            }

            final var claims = signedJwt.getJWTClaimsSet();
            final var sub = claims.getSubject();
            final var azp = claims.getStringClaim(CLAIM_AZP) != null
                    ? claims.getStringClaim(CLAIM_AZP)
                    : claims.getAudience().isEmpty() ? null : claims.getAudience().get(0);

            if (sub == null || azp == null) {
                throw new OAuth2Exception(ERROR_INVALID_REQUEST, "id_token_hint carries no sub/azp.");
            }

            return new String[]{sub, azp};

        } catch (ParseException | JOSEException ex) {
            throw new OAuth2Exception(ERROR_INVALID_REQUEST, "id_token_hint is not a verifiable id_token.", ex);
        }

    }

    private OidcClientRegistration authenticateClient(final OidcProviderTokenRequest request,
                                                      final OidcClientRegistration.GrantType grantType) {

        if (request.getClientId() == null || request.getClientSecret() == null) {
            throw new OAuth2Exception(ERROR_INVALID_CLIENT, "Client authentication required.");
        }

        if (!getOidcProviderDao().verifyClientSecret(request.getClientId(), request.getClientSecret())) {
            throw new OAuth2Exception(ERROR_INVALID_CLIENT, "Invalid client credentials.");
        }

        final var registration = getOidcProviderDao().getClient(request.getClientId())
                .orElseThrow(() -> new OAuth2Exception(ERROR_INVALID_CLIENT, "Unknown client."));

        if (registration.getStatus() != OidcClientRegistration.Status.ACTIVE) {
            throw new OAuth2Exception(ERROR_UNAUTHORIZED_CLIENT, "Client registration is not active.");
        }

        if (!registration.getGrantTypes().contains(grantType)) {
            throw new OAuth2Exception(ERROR_UNAUTHORIZED_CLIENT, "Grant type not allowed for this client.");
        }

        return registration;

    }

    private void validateCode(final OidcProviderTokenRequest request,
                              final OidcClientRegistration registration,
                              final OidcAuthorizationCode code) {

        if (!code.getClientId().equals(registration.getClientId())) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "Authorization code was not issued to this client.");
        }

        if (!code.getRedirectUri().equals(request.getRedirectUri())) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT,
                    "redirect_uri does not match the authorization request.");
        }

        if (code.getUserId() == null) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "Authorization code was never completed.");
        }

        // PKCE is mandatory: every issued code carries an S256 challenge.
        verifyPkce(code.getCodeChallengeMethod(), code.getCodeChallenge(), request.getCodeVerifier());

    }

    private void verifyPkce(final String method, final String challenge, final String verifier) {

        if (!CODE_CHALLENGE_METHOD_S256.equals(method) || challenge == null || verifier == null) {
            throw new OAuth2Exception(ERROR_INVALID_GRANT, "PKCE verification failed.");
        }

        try {
            final var digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            final var computed = Base64URL.encode(digest).toString();
            if (!computed.equals(challenge)) {
                throw new OAuth2Exception(ERROR_INVALID_GRANT, "PKCE verification failed.");
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new InternalException(ex);
        }

    }

    private SessionCreation issueSession(final User user, final String applicationId) {

        final var session = new Session();
        session.setUser(user);
        session.setExpiry(currentTimeMillis() + getSessionTimeoutSeconds() * 1000);

        if (applicationId != null) {
            getApplicationDao().findApplication(applicationId).ifPresent(application -> {
                session.setApplication(application);
                getProfileDao().findPrimaryProfile(user.getId(), applicationId).ifPresent(session::setProfile);
            });
        }

        return getSessionTokenIssuer().issue(session);

    }

    private String issueRefreshToken(final OidcProviderGrant grant, final OidcClientRegistration registration) {

        final var bytes = new byte[REFRESH_TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        final var rawToken = Base64URL.encode(bytes).toString();

        final var record = new OidcRefreshToken();
        record.setSecretHash(sha256Hex(rawToken));
        record.setGrantId(grant.getId());
        record.setClientId(registration.getClientId());
        record.setStatus(OidcRefreshToken.Status.ACTIVE);
        record.setExpiry(grant.getExpiry());

        getOidcProviderDao().createRefreshToken(record);

        return rawToken;

    }

    private String signIdToken(final String clientId, final User user, final String nonce, final long authTime) {

        final var now = currentTimeMillis();
        final var expiry = now + getIdTokenTtlSeconds() * 1000;

        final var claimsBuilder = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer(getIssuer())
                .audience(clientId)
                .claim(CLAIM_AZP, clientId)
                .issueTime(new Date(now))
                .expirationTime(new Date(expiry));

        if (nonce != null) {
            claimsBuilder.claim("nonce", nonce);
        }

        if (authTime > 0) {
            claimsBuilder.claim("auth_time", authTime / 1000);
        }

        return signToken(claimsBuilder.build());

    }

    /**
     * Signs the supplied claim set with this deployment's active signing key, producing a compact RS256
     * JWS with the key's {@code kid} in the header.
     */
    private String signToken(final JWTClaimsSet claims) {

        try {

            final var signingKey = getSessionTokenKeySupport().getActiveSigningKey();
            final var privateKey = (RSAPrivateKey) getSessionTokenKeySupport().getPrivateKey(signingKey);

            final var signedJwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKid()).build(),
                    claims);

            signedJwt.sign(new RSASSASigner(privateKey));

            return signedJwt.serialize();

        } catch (JOSEException ex) {
            throw new InternalException(ex);
        }

    }

    private String requireSessionId(final SessionCreation sessionCreation) {
        return getSessionTokenVerifier().verify(sessionCreation.getSessionSecret()).getSessionId();
    }

    private String sha256Hex(final String value) {
        try {
            final var digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new InternalException(ex);
        }
    }

    public OidcProviderDao getOidcProviderDao() {
        return oidcProviderDao;
    }

    @Inject
    public void setOidcProviderDao(OidcProviderDao oidcProviderDao) {
        this.oidcProviderDao = oidcProviderDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public SessionTokenIssuer getSessionTokenIssuer() {
        return sessionTokenIssuer;
    }

    @Inject
    public void setSessionTokenIssuer(SessionTokenIssuer sessionTokenIssuer) {
        this.sessionTokenIssuer = sessionTokenIssuer;
    }

    public SessionTokenVerifier getSessionTokenVerifier() {
        return sessionTokenVerifier;
    }

    @Inject
    public void setSessionTokenVerifier(SessionTokenVerifier sessionTokenVerifier) {
        this.sessionTokenVerifier = sessionTokenVerifier;
    }

    public StandardSessionTokenKeySupport getSessionTokenKeySupport() {
        return sessionTokenKeySupport;
    }

    @Inject
    public void setSessionTokenKeySupport(StandardSessionTokenKeySupport sessionTokenKeySupport) {
        this.sessionTokenKeySupport = sessionTokenKeySupport;
    }

    public OidcProviderRateLimiter getRateLimiter() {
        return rateLimiter;
    }

    @Inject
    public void setRateLimiter(OidcProviderRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getIdTokenTtlSeconds() {
        return idTokenTtlSeconds;
    }

    public void setIdTokenTtlSeconds(long idTokenTtlSeconds) {
        this.idTokenTtlSeconds = idTokenTtlSeconds;
    }

    public long getRefreshGrantTtlSeconds() {
        return refreshGrantTtlSeconds;
    }

    public void setRefreshGrantTtlSeconds(long refreshGrantTtlSeconds) {
        this.refreshGrantTtlSeconds = refreshGrantTtlSeconds;
    }

    public long getMachineTokenTtlSeconds() {
        return machineTokenTtlSeconds;
    }

    public void setMachineTokenTtlSeconds(long machineTokenTtlSeconds) {
        this.machineTokenTtlSeconds = machineTokenTtlSeconds;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public void setSessionTimeoutSeconds(long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
