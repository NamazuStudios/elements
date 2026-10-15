package dev.getelements.elements.service.auth.oidc.provider;

import com.nimbusds.jose.util.Base64URL;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.auth.OAuth2Exception;
import dev.getelements.elements.sdk.model.oidc.OidcProviderAuthorizationRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderConsent;
import dev.getelements.elements.sdk.model.oidc.OidcProviderLoginRequest;
import dev.getelements.elements.sdk.service.auth.OidcProviderAuthorizationService;
import dev.getelements.elements.sdk.service.Constants;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Set;

import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.CODE_CHALLENGE_METHOD_S256;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_ACCESS_DENIED;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_REQUEST;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_INVALID_SCOPE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_UNAUTHORIZED_CLIENT;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.ERROR_UNSUPPORTED_RESPONSE_TYPE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.RESPONSE_TYPE_CODE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_OPENID;
import static dev.getelements.elements.sdk.service.Constants.OIDC_AUTHORIZATION_CODE_TTL_SECONDS;
import static dev.getelements.elements.sdk.service.Constants.OIDC_PROVIDER_CONSENT_REQUIRED;

public class StandardOidcProviderAuthorizationService implements OidcProviderAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(StandardOidcProviderAuthorizationService.class);

    private static final int CODE_BYTES = 32;

    private OidcProviderDao oidcProviderDao;

    private UserDao userDao;

    private OidcProviderRateLimiter rateLimiter;

    @Named(OIDC_AUTHORIZATION_CODE_TTL_SECONDS)
    @Inject
    private long authorizationCodeTtlSeconds;

    @Named(OIDC_PROVIDER_CONSENT_REQUIRED)
    @Inject
    private boolean consentRequired;

    @Override
    public OidcAuthorizationCode beginAuthorization(final OidcProviderAuthorizationRequest request) {
        getRateLimiter().checkAuthorizationEndpoint(request.getClientId());
        return getOidcProviderDao().createAuthorizationCode(newAuthorizationCodeRecord(request, null, 0));
    }

    @Override
    public OidcProviderConsent submitLogin(final OidcProviderLoginRequest loginRequest) {

        getRateLimiter().checkHostedLogin(loginRequest.getAuthorizationId(), loginRequest.getUsername());

        final var user = getUserDao().validateUserPassword(
                loginRequest.getUsername(), loginRequest.getPassword());

        // Atomic attach: the user id and auth_time land on the code exactly once, and the code enters
        // AWAITING_CONSENT. The consent decision (explicit or implicit) restores PENDING.
        final var code = getOidcProviderDao().completeAuthorizationCode(
                loginRequest.getAuthorizationId(),
                user.getId(),
                System.currentTimeMillis()
        ).orElseThrow(() -> new InvalidDataException("Unknown or already used authorization request."));

        final var registration = getOidcProviderDao().getClient(code.getClientId())
                .orElseThrow(() -> new InvalidDataException("Unknown client."));

        if (!getConsentRequired()) {
            return complete(code, true);
        }

        logger.info("Awaiting consent from user {} for client {}.", user.getId(), code.getClientId());

        final var consent = new OidcProviderConsent();
        consent.setAuthorizationId(code.getCode());
        consent.setConsentRequired(true);
        consent.setClientDisplayName(registration.getDisplayName());
        consent.setScope(code.getScope());
        return consent;

    }

    @Override
    public String approveConsent(final String authorizationId) {
        final var code = decide(authorizationId, true);
        logger.info("Consent granted for user {} on client {}.", code.getUserId(), code.getClientId());
        return buildRedirectUri(code);
    }

    @Override
    public String denyConsent(final String authorizationId) {

        final var code = decide(authorizationId, false);

        logger.info("Consent denied by user {} on client {}.", code.getUserId(), code.getClientId());

        // Per RFC 6749 section 4.1.2.1 the denial is reported back to the relying party as an
        // access_denied error, echoing the client's state.

        final var builder = new StringBuilder(code.getRedirectUri());
        builder.append(code.getRedirectUri().contains("?") ? '&' : '?');
        builder.append("error=").append(ERROR_ACCESS_DENIED);

        if (code.getState() != null) {
            builder.append("&state=").append(code.getState());
        }

        return builder.toString();

    }

    private OidcProviderConsent complete(final OidcAuthorizationCode code, final boolean approved) {

        final var decided = decide(code.getCode(), approved);

        final var consent = new OidcProviderConsent();
        consent.setAuthorizationId(code.getCode());
        consent.setConsentRequired(false);
        consent.setScope(decided.getScope());
        consent.setRedirectUri(buildRedirectUri(decided));
        return consent;

    }

    private OidcAuthorizationCode decide(final String authorizationId, final boolean approved) {
        return getOidcProviderDao().decideConsent(authorizationId, approved)
                .orElseThrow(() -> new InvalidDataException("Unknown or already decided authorization request."));
    }

    private OidcAuthorizationCode newAuthorizationCodeRecord(final OidcProviderAuthorizationRequest request,
                                                             final String userId,
                                                             final long authTime) {

        final var registration = validateAuthorizationRequest(request);

        final var code = new OidcAuthorizationCode();
        code.setCode(generateCode());
        code.setClientId(registration.getClientId());
        code.setUserId(userId);
        code.setApplicationId(registration.getApplicationId());
        code.setRedirectUri(request.getRedirectUri());
        code.setScope(request.getScope());
        code.setState(request.getState());
        code.setNonce(request.getNonce());
        code.setCodeChallenge(request.getCodeChallenge());
        code.setCodeChallengeMethod(request.getCodeChallengeMethod());
        code.setStatus(OidcAuthorizationCode.Status.PENDING);
        code.setAuthTime(authTime);
        code.setExpiry(new Timestamp(System.currentTimeMillis() + getAuthorizationCodeTtlSeconds() * 1000));

        return code;

    }

    private OidcClientRegistration validateAuthorizationRequest(final OidcProviderAuthorizationRequest request) {

        // Errors raised before the client and redirect URI are validated carry no redirect context: per
        // RFC 6749 section 4.1.2.1 the provider must NOT bounce the browser to an unvalidated URI.

        if (!RESPONSE_TYPE_CODE.equals(request.getResponseType())) {
            throw new OAuth2Exception(ERROR_UNSUPPORTED_RESPONSE_TYPE,
                    "Unsupported response_type; only \"code\" is supported.");
        }

        if (!CODE_CHALLENGE_METHOD_S256.equals(request.getCodeChallengeMethod())) {
            throw new OAuth2Exception(ERROR_INVALID_REQUEST,
                    "Unsupported code_challenge_method; only \"S256\" is supported.");
        }

        final var registration = getOidcProviderDao().getClient(request.getClientId())
                .orElseThrow(() -> new OAuth2Exception(ERROR_INVALID_REQUEST, "Unknown client."));

        if (registration.getStatus() != OidcClientRegistration.Status.ACTIVE) {
            throw new OAuth2Exception(ERROR_UNAUTHORIZED_CLIENT, "Client registration is not active.");
        }

        if (!registration.getRedirectUris().contains(request.getRedirectUri())) {
            throw new OAuth2Exception(ERROR_INVALID_REQUEST, "Redirect URI is not registered for this client.");
        }

        // Past this point the redirect URI is registered, so the error is redirect-able: attach the context
        // the resource needs to send the browser back to the relying party with the error parameters.

        try {

            final var requestedScopes = Set.of(request.getScope().trim().split("\\s+"));

            if (!requestedScopes.contains(SCOPE_OPENID)) {
                throw new OAuth2Exception(ERROR_INVALID_SCOPE, "The openid scope is required.");
            }

            if (!registration.getScopes().containsAll(requestedScopes)) {
                throw new OAuth2Exception(ERROR_INVALID_SCOPE,
                        "Requested scope exceeds the client's allowed scopes.");
            }

        } catch (final OAuth2Exception ex) {
            ex.setRedirectUri(request.getRedirectUri());
            ex.setState(request.getState());
            throw ex;
        }

        return registration;

    }

    private String buildRedirectUri(final OidcAuthorizationCode code) {

        // The redirect URI was exact-matched against the registration before the code was issued, so
        // appending code/state here is safe from open-redirect manipulation.

        final var builder = new StringBuilder(code.getRedirectUri());
        builder.append(code.getRedirectUri().contains("?") ? '&' : '?');
        builder.append("code=").append(code.getCode());

        if (code.getState() != null) {
            builder.append("&state=").append(code.getState());
        }

        return builder.toString();

    }

    private String generateCode() {
        final var bytes = new byte[CODE_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64URL.encode(bytes).toString();
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

    public OidcProviderRateLimiter getRateLimiter() {
        return rateLimiter;
    }

    @Inject
    public void setRateLimiter(OidcProviderRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public long getAuthorizationCodeTtlSeconds() {
        return authorizationCodeTtlSeconds;
    }

    public void setAuthorizationCodeTtlSeconds(long authorizationCodeTtlSeconds) {
        this.authorizationCodeTtlSeconds = authorizationCodeTtlSeconds;
    }

    public boolean getConsentRequired() {
        return consentRequired;
    }

    public void setConsentRequired(boolean consentRequired) {
        this.consentRequired = consentRequired;
    }

}
