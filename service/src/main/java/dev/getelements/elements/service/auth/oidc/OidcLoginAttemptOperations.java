package dev.getelements.elements.service.auth.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.OidcLoginAttemptDao;
import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttempt;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttemptStatus;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.auth.TokenEndpointAuthMethod;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptBegin;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptCallbackResult;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;

import static dev.getelements.elements.sdk.service.Constants.OIDC_LOGIN_ATTEMPT_TTL_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Orchestrates the provider-agnostic, browser-redirect OIDC login flow: starting a pending attempt, polling for
 * completion, and handling the provider's callback. All state (id/state/nonce), the code exchange, and
 * id_token validation happen here, server-side, per the design — the client only ever sees an opaque id and
 * the Elements session it eventually resolves to.
 *
 * <p>Supports both anonymous (first-time login) and account-linking attempts. Which behavior applies is decided
 * once, at {@link #begin(String, User)} time, when the caller's own session (if any) is known. For an anonymous
 * attempt, {@link #handleCallback} builds and stores the session directly, exactly as before. For a linking
 * attempt, {@link #handleCallback} — always hit by an unauthenticated provider redirect, with no session of its
 * own and no way to verify it's talking to the same party that called {@code begin()} — deliberately does
 * <em>not</em> perform the account-link mutation. It only validates the external identity and marks the attempt
 * {@link OidcLoginAttemptStatus#LINK_READY}. Finalizing it requires {@link #confirmLink}, presenting the
 * {@code confirmToken} returned only in the original {@code begin()} response — proof that the caller is the
 * same party that started the attempt, without relying on Elements' own session/access-level machinery.
 */
public class OidcLoginAttemptOperations {

    private static final Logger logger = LoggerFactory.getLogger(OidcLoginAttemptOperations.class);

    private static final int TOKEN_BYTES = 32;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OidcProviderConfigurationDao oidcProviderConfigurationDao;

    private OidcLoginAttemptDao oidcLoginAttemptDao;

    private OidcProviderConfigurationOperations oidcProviderConfigurationOperations;

    private OidcAuthServiceOperations oidcAuthServiceOperations;

    private AnonOidcAuthService anonOidcAuthService;

    private UserOidcAuthService userOidcAuthService;

    private UserDao userDao;

    private Client client;

    private long ttlSeconds;

    public OidcLoginAttemptBegin begin(final String provider) {
        return begin(provider, null);
    }

    /**
     * Begins a pending login attempt, optionally on behalf of an already-authenticated user. When
     * {@code linkingUser} is non-null (the caller had an existing Elements session when starting the attempt),
     * the attempt is stamped with that user's id so a later successful callback links the external identity to
     * them instead of creating/finding a user by external id — the callback itself, hit by an unauthenticated
     * provider redirect, has no session of its own to make that determination.
     *
     * @param provider the provider identifier
     * @param linkingUser the already-authenticated user to link on success, or {@code null} for an anonymous
     *                    (first-time login) attempt
     * @return the pending attempt's id, authorize URL, and expiry
     */
    public OidcLoginAttemptBegin begin(final String provider, final User linkingUser) {

        final var config = findConfigOrThrow(provider);
        final var discoveryDocument = getOidcProviderConfigurationOperations().resolveDiscovery(config);

        final var id = randomToken();
        final var state = randomToken();
        final var nonce = randomToken();
        final var confirmToken = randomToken();
        final var expiry = new Timestamp(currentTimeMillis() + (ttlSeconds * 1000));

        final var attempt = new OidcLoginAttempt();
        attempt.setId(id);
        attempt.setProvider(provider);
        attempt.setState(state);
        attempt.setNonce(nonce);
        attempt.setConfirmToken(confirmToken);
        attempt.setStatus(OidcLoginAttemptStatus.PENDING);
        attempt.setExpiry(expiry);
        // Snapshotted from the provider config at begin() time, not read live at callback time, so an admin
        // edit to the config mid-flight can't change the outcome of an attempt already in progress.
        attempt.setSuccessRedirectUrl(config.getSuccessRedirectUrl());
        attempt.setErrorRedirectUrl(config.getErrorRedirectUrl());

        if (linkingUser != null) {
            attempt.setLinkedUserId(linkingUser.getId());
        }

        getOidcLoginAttemptDao().create(attempt);

        final var authorizeUrl = buildAuthorizeUrl(config, discoveryDocument, state, nonce);

        return new OidcLoginAttemptBegin(id, authorizeUrl, expiry.toInstant().getEpochSecond(), confirmToken);

    }

    public OidcLoginAttemptStatusResponse poll(final String id) {

        final var claimed = getOidcLoginAttemptDao().claimCompleteById(id);

        if (claimed.isPresent()) {
            final var sessionCreation = deserializeSession(claimed.get().getSessionToken());
            return OidcLoginAttemptStatusResponse.complete(sessionCreation);
        }

        // A non-mutating peek: a linking attempt is never finalizable via plain poll, since doing so would mean
        // trusting whoever presents `id` alone with the account-link mutation. Reject rather than silently
        // returning PENDING forever, so the client learns it must call confirmLink() instead.
        if (getOidcLoginAttemptDao().findLinkReadyById(id).isPresent()) {
            throw new ForbiddenException("This login attempt requires confirmation via POST .../confirm.");
        }

        return getOidcLoginAttemptDao()
                .findPendingOrFailedById(id)
                .map(attempt -> attempt.getStatus() == OidcLoginAttemptStatus.FAILED
                        ? OidcLoginAttemptStatusResponse.failed(attempt.getFailureReason())
                        : OidcLoginAttemptStatusResponse.pending())
                // Unknown id, already claimed, or TTL-purged are all indistinguishable to the caller, and
                // all correctly reported as EXPIRED per the API contract.
                .orElseGet(OidcLoginAttemptStatusResponse::expired);

    }

    /**
     * Finalizes a linking attempt by presenting the {@code confirmToken} returned only in the original
     * {@code begin()} response — the sole proof that the caller is the same party that started the attempt.
     * Performs the actual account-link mutation (deferred out of {@link #handleCallback}, which cannot make this
     * determination itself) and mints the resulting session.
     *
     * @param id the opaque poll id
     * @param confirmToken the secret returned in the original {@code begin()} response for this attempt
     * @return the completed session, or {@link OidcLoginAttemptStatusResponse#expired()} if the attempt is
     *         unknown, not link-ready, already claimed, or expired
     */
    public OidcLoginAttemptStatusResponse confirmLink(final String id, final String confirmToken) {

        final var attemptOptional = getOidcLoginAttemptDao().findLinkReadyById(id);

        if (attemptOptional.isEmpty()) {
            return OidcLoginAttemptStatusResponse.expired();
        }

        final var attempt = attemptOptional.get();

        if (confirmToken == null || !constantTimeEquals(confirmToken, attempt.getConfirmToken())) {
            // Deliberately does not consume the attempt: a wrong/missing token might just be a client bug, and
            // the legitimate holder of the real confirmToken must still be able to retry.
            throw new ForbiddenException("Invalid confirmation token.");
        }

        // Only claim (and thus consume) the attempt once the token has already been verified to match.
        final var claimed = getOidcLoginAttemptDao().claimLinkReadyById(id);

        if (claimed.isEmpty()) {
            // Lost a race to a concurrent confirm for the same id.
            return OidcLoginAttemptStatusResponse.expired();
        }

        final var claims = deserializeLinkClaims(claimed.get().getLinkClaimsJson());
        final var targetUser = getUserDao().getUser(claimed.get().getLinkedUserId());

        final var linkedUser = getUserOidcAuthService().apply(
                targetUser, claims.getSchemeName(), claims.getExternalUserId(), claims.getEmail(),
                claims.getProfileClaims());

        final var sessionCreation = getOidcAuthServiceOperations().createSessionForResolvedUser(linkedUser);

        return OidcLoginAttemptStatusResponse.complete(sessionCreation);

    }

    public OidcLoginAttemptCallbackResult handleCallback(final String provider, final String code,
                                                          final String state, final String error) {

        // Looked up unconditionally, before any failure branch, so the caller-configured redirect URLs are known
        // regardless of which outcome this callback resolves to — including the "provider reported an error" and
        // "unknown attempt" cases below, neither of which used to have access to them.
        final var attemptOptional = getOidcLoginAttemptDao().findPendingByState(provider, state);
        final var successRedirectUrl = attemptOptional.map(OidcLoginAttempt::getSuccessRedirectUrl).orElse(null);
        final var errorRedirectUrl = attemptOptional.map(OidcLoginAttempt::getErrorRedirectUrl).orElse(null);

        try {

            if (error != null) {
                logger.debug("OIDC callback for provider {} reported an error", provider);
                getOidcLoginAttemptDao().markFailed(state, "Login was not completed");
                return OidcLoginAttemptCallbackResult.failure(errorRedirectUrl);
            }

            final var attempt = attemptOptional
                    .orElseThrow(() -> new ForbiddenException("Unknown or expired login attempt"));

            final var config = findConfigOrThrow(provider);
            final var discoveryDocument = getOidcProviderConfigurationOperations().resolveDiscovery(config);
            final var scheme = getOidcProviderConfigurationOperations().resolveScheme(config, discoveryDocument);

            final var idToken = exchangeCodeForIdToken(config, discoveryDocument, code);

            final var decodedJWT = getOidcAuthServiceOperations().decodeAndVerify(
                    idToken, scheme, config.getClientId(), attempt.getNonce());

            if (attempt.getLinkedUserId() != null) {

                // Linking attempt: the account-link mutation is deliberately deferred to confirmLink(), which
                // this (always-unauthenticated) callback cannot itself gate correctly -- it has no way to verify
                // it's talking to the same party that called begin(). Only the validated external identity is
                // persisted here.
                final var claims = new OidcLinkClaims();
                claims.setSchemeName(scheme.getName());
                claims.setExternalUserId(OidcAuthServiceOperations.claimAsString(
                        decodedJWT, OidcAuthServiceOperations.Claim.USER_ID.value));
                claims.setEmail(OidcAuthServiceOperations.claimAsString(
                        decodedJWT, OidcAuthServiceOperations.Claim.EMAIL.value));
                claims.setProfileClaims(OidcAuthServiceOperations.extractProfileClaims(decodedJWT));

                final var linkReady = getOidcLoginAttemptDao().markLinkReady(state, serializeLinkClaims(claims));

                if (linkReady.isEmpty()) {
                    return OidcLoginAttemptCallbackResult.failure(errorRedirectUrl);
                }

                return OidcLoginAttemptCallbackResult.success(successRedirectUrl);

            }

            final var sessionCreation = getOidcAuthServiceOperations().createOrUpdateUserWithVerifiedToken(
                    decodedJWT, scheme, getAnonOidcAuthService()::apply);

            final var completed = getOidcLoginAttemptDao().markComplete(state, serializeSession(sessionCreation));

            if (completed.isEmpty()) {
                // Guard did not match: either a replayed callback for an already-resolved attempt, or a
                // concurrent duplicate delivery for the same state. Fail closed rather than silently succeeding
                // twice.
                return OidcLoginAttemptCallbackResult.failure(errorRedirectUrl);
            }

            return OidcLoginAttemptCallbackResult.success(successRedirectUrl);

        } catch (final Exception ex) {
            // Never log the code, id_token, or attempt id — only enough context to debug which provider/attempt failed.
            // Every expected failure mode is caught here so the REST layer never has to interpret an exception —
            // it only sees a plain failure result.
            logger.debug("OIDC callback failed for provider {}", provider, ex);
            getOidcLoginAttemptDao().markFailed(state, "Login failed");
            return OidcLoginAttemptCallbackResult.failure(errorRedirectUrl);
        }

    }

    private OidcProviderConfiguration findConfigOrThrow(final String provider) {
        return getOidcProviderConfigurationDao()
                .findByName(provider)
                .orElseThrow(() -> new InvalidDataException("Unknown provider: " + provider));
    }

    private String exchangeCodeForIdToken(final OidcProviderConfiguration config,
                                           final OidcDiscoveryDocument discoveryDocument,
                                           final String code) {

        // client_id is always sent in the body, even under CLIENT_SECRET_BASIC — some providers (e.g. Twitch)
        // don't read it from the Authorization header and fail with "missing client id" if it's only sent
        // there. client_secret placement stays conditional on tokenEndpointAuthMethod: it's not safe to assume
        // every provider tolerates it in both places at once.
        final var form = new Form()
                .param("grant_type", "authorization_code")
                .param("code", code)
                .param("redirect_uri", config.getRedirectUri())
                .param("client_id", config.getClientId());

        var target = getClient().target(discoveryDocument.getTokenEndpoint());
        var requestBuilder = target.request(MediaType.APPLICATION_JSON);

        if (config.getTokenEndpointAuthMethod() == TokenEndpointAuthMethod.CLIENT_SECRET_POST) {
            form.param("client_secret", config.getClientSecret());
        } else {
            final var credentials = config.getClientId() + ":" + config.getClientSecret();
            final var basic = Base64.getEncoder().encodeToString(credentials.getBytes(UTF_8));
            requestBuilder = requestBuilder.header("Authorization", "Basic " + basic);
        }

        final var response = requestBuilder.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        try {

            if (response.getStatus() != 200) {
                final var errorResponse = response.readEntity(String.class);
                logger.error("Error from OIDC Provider {} {}.", config.getName(), errorResponse);
                throw new ForbiddenException("Token exchange failed with status " + response.getStatus());
            }

            final var tokenResponse = response.readEntity(OidcTokenResponse.class);

            if (tokenResponse.getIdToken() == null) {
                throw new ForbiddenException("Token endpoint response did not contain an id_token");
            }

            return tokenResponse.getIdToken();

        } finally {
            response.close();
        }

    }

    private String buildAuthorizeUrl(final OidcProviderConfiguration config,
                                      final OidcDiscoveryDocument discoveryDocument,
                                      final String state,
                                      final String nonce) {

        final var sb = new StringBuilder(discoveryDocument.getAuthorizationEndpoint());
        sb.append(discoveryDocument.getAuthorizationEndpoint().contains("?") ? '&' : '?');
        sb.append("response_type=code");
        sb.append("&client_id=").append(encode(config.getClientId()));
        sb.append("&redirect_uri=").append(encode(config.getRedirectUri()));
        sb.append("&state=").append(encode(state));
        sb.append("&nonce=").append(encode(nonce));

        if (config.getScopes() != null && !config.getScopes().isEmpty()) {
            sb.append("&scope=").append(encode(String.join(" ", config.getScopes())));
        }

        if (config.getExtraAuthorizeParams() != null) {
            config.getExtraAuthorizeParams().forEach((key, value) ->
                    sb.append('&').append(encode(key)).append('=').append(encode(value)));
        }

        return sb.toString();

    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String randomToken() {
        final var bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String serializeSession(final SessionCreation sessionCreation) {
        try {
            return OBJECT_MAPPER.writeValueAsString(sessionCreation);
        } catch (final Exception ex) {
            throw new InternalException(ex);
        }
    }

    private static SessionCreation deserializeSession(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, SessionCreation.class);
        } catch (final Exception ex) {
            throw new InternalException(ex);
        }
    }

    private static String serializeLinkClaims(final OidcLinkClaims claims) {
        try {
            return OBJECT_MAPPER.writeValueAsString(claims);
        } catch (final Exception ex) {
            throw new InternalException(ex);
        }
    }

    private static OidcLinkClaims deserializeLinkClaims(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, OidcLinkClaims.class);
        } catch (final Exception ex) {
            throw new InternalException(ex);
        }
    }

    /**
     * A real secret comparison (unlike the other lookups in this class, which are keyed by unguessable random
     * tokens anyway) — compared in constant time to avoid leaking match-length information via timing.
     */
    private static boolean constantTimeEquals(final String a, final String b) {
        return MessageDigest.isEqual(a.getBytes(UTF_8), b.getBytes(UTF_8));
    }

    public OidcProviderConfigurationDao getOidcProviderConfigurationDao() {
        return oidcProviderConfigurationDao;
    }

    @Inject
    public void setOidcProviderConfigurationDao(OidcProviderConfigurationDao oidcProviderConfigurationDao) {
        this.oidcProviderConfigurationDao = oidcProviderConfigurationDao;
    }

    public OidcLoginAttemptDao getOidcLoginAttemptDao() {
        return oidcLoginAttemptDao;
    }

    @Inject
    public void setOidcLoginAttemptDao(OidcLoginAttemptDao oidcLoginAttemptDao) {
        this.oidcLoginAttemptDao = oidcLoginAttemptDao;
    }

    public OidcProviderConfigurationOperations getOidcProviderConfigurationOperations() {
        return oidcProviderConfigurationOperations;
    }

    @Inject
    public void setOidcProviderConfigurationOperations(OidcProviderConfigurationOperations oidcProviderConfigurationOperations) {
        this.oidcProviderConfigurationOperations = oidcProviderConfigurationOperations;
    }

    public OidcAuthServiceOperations getOidcAuthServiceOperations() {
        return oidcAuthServiceOperations;
    }

    @Inject
    public void setOidcAuthServiceOperations(OidcAuthServiceOperations oidcAuthServiceOperations) {
        this.oidcAuthServiceOperations = oidcAuthServiceOperations;
    }

    public AnonOidcAuthService getAnonOidcAuthService() {
        return anonOidcAuthService;
    }

    @Inject
    public void setAnonOidcAuthService(AnonOidcAuthService anonOidcAuthService) {
        this.anonOidcAuthService = anonOidcAuthService;
    }

    public UserOidcAuthService getUserOidcAuthService() {
        return userOidcAuthService;
    }

    @Inject
    public void setUserOidcAuthService(UserOidcAuthService userOidcAuthService) {
        this.userOidcAuthService = userOidcAuthService;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    @Inject
    public void setTtlSeconds(@Named(OIDC_LOGIN_ATTEMPT_TTL_SECONDS) long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

}
