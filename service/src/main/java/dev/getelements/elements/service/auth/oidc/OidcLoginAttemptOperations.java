package dev.getelements.elements.service.auth.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.OidcLoginAttemptDao;
import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
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

    private Client client;

    private long ttlSeconds;

    public OidcLoginAttemptBegin begin(final String provider) {

        final var config = findConfigOrThrow(provider);
        final var discoveryDocument = getOidcProviderConfigurationOperations().resolveDiscovery(config);

        final var id = randomToken();
        final var state = randomToken();
        final var nonce = randomToken();
        final var expiry = new Timestamp(currentTimeMillis() + (ttlSeconds * 1000));

        final var attempt = new OidcLoginAttempt();
        attempt.setId(id);
        attempt.setProvider(provider);
        attempt.setState(state);
        attempt.setNonce(nonce);
        attempt.setStatus(OidcLoginAttemptStatus.PENDING);
        attempt.setExpiry(expiry);
        // Snapshotted from the provider config at begin() time, not read live at callback time, so an admin
        // edit to the config mid-flight can't change the outcome of an attempt already in progress.
        attempt.setSuccessRedirectUrl(config.getSuccessRedirectUrl());
        attempt.setErrorRedirectUrl(config.getErrorRedirectUrl());

        getOidcLoginAttemptDao().create(attempt);

        final var authorizeUrl = buildAuthorizeUrl(config, discoveryDocument, state, nonce);

        return new OidcLoginAttemptBegin(id, authorizeUrl, expiry.toInstant().getEpochSecond());

    }

    public OidcLoginAttemptStatusResponse poll(final String id) {

        final var claimed = getOidcLoginAttemptDao().claimCompleteById(id);

        if (claimed.isPresent()) {
            final var sessionCreation = deserializeSession(claimed.get().getSessionToken());
            return OidcLoginAttemptStatusResponse.complete(sessionCreation);
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
