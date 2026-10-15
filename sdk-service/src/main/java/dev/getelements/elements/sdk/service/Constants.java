package dev.getelements.elements.sdk.service;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementPublic;

/**
 * Service layer constants.
 */
@ElementPublic
public interface Constants {

    /**
     * Generic name for service scoped types.
     */
    String SERVICE = "dev.getelements.elements.service";

    /**
     * Names a service as unscoped.
     */
    String UNSCOPED = "dev.getelements.elements.service.unscoped";

    /**
     * Names a service as scoped to the ANONYMOUS user level.
     */
    String ANONYMOUS = "dev.getelements.elements.service.scope.anonymous";

    /**
     * Names a service as scoped to the USER user level.
     */
    String USER = "dev.getelements.elements.service.scope.user";

    /**
     * Names a service as scoped to the SUPERUSER user level.
     */
    String SUPERUSER = "dev.getelements.elements.service.scope.superuser";

    /**
     * Used to specify the RPC provider for bsc blockchain.
     */
    @ElementDefaultAttribute(
            value = "https://data-seed-prebsc-1-s1.binance.org:8545",
            description = "The RPC provider URL for the BSC blockchain."
    )
    String BSC_RPC_PROVIDER = "dev.getelements.elements.blockchain.bsc.provider";

    /**
     * Used to specify the session timeout, in seconds
     */
    @ElementDefaultAttribute(
            value = "172800",
            description = "The session timeout duration, in seconds."
    )
    String SESSION_TIMEOUT_SECONDS = "dev.getelements.elements.session.timeout.seconds";

    /**
     * Used to specify the mock session timeout.
     */
    @ElementDefaultAttribute(
            value = "3600",
            description = "The mock session timeout duration, in seconds."
    )
    String MOCK_SESSION_TIMEOUT_SECONDS = "dev.getelements.elements.mock.session.timeout.seconds";

    /**
     * Used to specify how long a pending browser-redirect OIDC login attempt remains valid.
     */
    @ElementDefaultAttribute(
            value = "300",
            description = "The lifetime of a pending OIDC login attempt, in seconds."
    )
    String OIDC_LOGIN_ATTEMPT_TTL_SECONDS = "dev.getelements.elements.oidc.login.attempt.ttl.seconds";

    /**
     * Used to specify how often a cached OIDC provider discovery document is refreshed.
     */
    @ElementDefaultAttribute(
            value = "3600",
            description = "How often a cached OIDC provider discovery document is refreshed, in seconds."
    )
    String OIDC_DISCOVERY_REFRESH_SECONDS = "dev.getelements.elements.oidc.discovery.refresh.seconds";

    /**
     * Used to specify how often cached OIDC scheme's JWKS keys are refreshed.
     */
    @ElementDefaultAttribute(
            value = "3600",
            description = "How often cached OIDC JWKS keys are refreshed, in seconds."
    )
    String OIDC_JWKS_REFRESH_SECONDS = "dev.getelements.elements.oidc.jwks.refresh.seconds";

    /**
     * Used to specify the issuer (iss) claim placed on Elements-issued session tokens. Tokens are
     * rejected whose iss does not match.
     */
    @ElementDefaultAttribute(
            value = "elements",
            description = "The issuer (iss) claim placed on Elements-issued session tokens."
    )
    String SESSION_TOKEN_ISSUER = "dev.getelements.elements.session.token.issuer";

    /**
     * Used to specify the base issuer URL advertised by the Elements OIDC provider's discovery document.
     * The discovery document is served at {@code <issuer>/.well-known/openid-configuration}, so this must
     * be set to the externally-reachable base URL of the REST API (e.g. {@code https://host/api/rest}).
     */
    @ElementDefaultAttribute(
            value = "http://localhost:8080/api/rest",
            description = "The base issuer URL advertised by the OIDC provider discovery document."
    )
    String OIDC_PROVIDER_ISSUER = "dev.getelements.elements.oidc.provider.issuer";

    /**
     * Used to specify how long an issued provider authorization code remains redeemable.
     */
    @ElementDefaultAttribute(
            value = "600",
            description = "The lifetime of an OIDC provider authorization code, in seconds."
    )
    String OIDC_AUTHORIZATION_CODE_TTL_SECONDS = "dev.getelements.elements.oidc.authorization.code.ttl.seconds";

    /**
     * Used to specify the lifetime of id_tokens issued by the Elements OIDC provider.
     */
    @ElementDefaultAttribute(
            value = "3600",
            description = "The lifetime of id_tokens issued by the OIDC provider, in seconds."
    )
    String OIDC_ID_TOKEN_TTL_SECONDS = "dev.getelements.elements.oidc.id.token.ttl.seconds";

    /**
     * Used to specify how long a grant (and its refresh token) remains valid. Refreshing does not extend
     * the grant: when it expires, the user must re-authenticate through the hosted login page.
     */
    @ElementDefaultAttribute(
            value = "2592000",
            description = "The lifetime of an OIDC provider grant and its refresh tokens, in seconds."
    )
    String OIDC_REFRESH_GRANT_TTL_SECONDS = "dev.getelements.elements.oidc.refresh.grant.ttl.seconds";

    /**
     * Used to specify the lifetime of machine-to-machine access tokens issued by the {@code client_credentials}
     * grant. These tokens are short-lived by design.
     */
    @ElementDefaultAttribute(
            value = "300",
            description = "The lifetime of OIDC provider client_credentials access tokens, in seconds."
    )
    String OIDC_MACHINE_TOKEN_TTL_SECONDS = "dev.getelements.elements.oidc.machine.token.ttl.seconds";

    /**
     * Whether the OIDC provider requires explicit end-user consent before completing an authorization. When
     * false, admin-registered clients are trusted implicitly (135a behavior).
     */
    @ElementDefaultAttribute(
            value = "true",
            description = "Whether the OIDC provider requires explicit end-user consent before granting access."
    )
    String OIDC_PROVIDER_CONSENT_REQUIRED = "dev.getelements.elements.oidc.provider.consent.required";

    /**
     * Whether rate limiting is enforced on the OIDC provider's authorization, token, and login endpoints.
     */
    @ElementDefaultAttribute(
            value = "true",
            description = "Whether rate limiting is enforced on the OIDC provider endpoints."
    )
    String OIDC_RATE_LIMIT_ENABLED = "dev.getelements.elements.oidc.rate.limit.enabled";

    /**
     * The maximum number of token endpoint requests permitted per client per minute.
     */
    @ElementDefaultAttribute(
            value = "60",
            description = "The maximum token endpoint requests permitted per client per minute."
    )
    String OIDC_RATE_LIMIT_TOKEN_PER_MINUTE = "dev.getelements.elements.oidc.rate.limit.token.per.minute";

    /**
     * The maximum number of authorization endpoint requests permitted per client per minute.
     */
    @ElementDefaultAttribute(
            value = "60",
            description = "The maximum authorization endpoint requests permitted per client per minute."
    )
    String OIDC_RATE_LIMIT_AUTHORIZE_PER_MINUTE = "dev.getelements.elements.oidc.rate.limit.authorize.per.minute";

    /**
     * The maximum number of hosted-login submissions permitted per client per minute, guarding the credential
     * entry step against brute force.
     */
    @ElementDefaultAttribute(
            value = "10",
            description = "The maximum hosted-login submissions permitted per client per minute."
    )
    String OIDC_RATE_LIMIT_LOGIN_PER_MINUTE = "dev.getelements.elements.oidc.rate.limit.login.per.minute";

    /**
     * Used to specify the audience (aud) claim placed on Elements-issued session tokens. Tokens are
     * rejected whose aud does not match.
     */
    @ElementDefaultAttribute(
            value = "elements",
            description = "The audience (aud) claim placed on Elements-issued session tokens."
    )
    String SESSION_TOKEN_AUDIENCE = "dev.getelements.elements.session.token.audience";

    /**
     * Used to specify the passphrase with which the session-token signing keys' private halves are
     * encrypted at rest. Operators should override this per deployment; the default is suitable only
     * for local development.
     */
    @ElementDefaultAttribute(
            value = "elements-default-signing-key-passphrase",
            description = "The passphrase used to encrypt session-token signing keys at rest. Override per deployment."
    )
    String SESSION_TOKEN_KEY_PASSPHRASE = "dev.getelements.elements.session.token.key.passphrase";

    /**
     * Used to specify how often the set of session-token signing keys is refreshed from the database,
     * so that key rotation is picked up without a restart.
     */
    @ElementDefaultAttribute(
            value = "300",
            description = "How often the set of session-token signing keys is refreshed, in seconds."
    )
    String SESSION_TOKEN_KEY_REFRESH_SECONDS = "dev.getelements.elements.session.token.key.refresh.seconds";

    /**
     * Used to specify the host for neo blockchain.
     */
    @ElementDefaultAttribute(
            value = "http://127.0.0.1",
            description = "The host URL for the NEO blockchain."
    )
    String NEO_BLOCKCHAIN_HOST = "dev.getelements.elements.blockchain.neo.host";

    /**
     * Used to specify the port for neo blockchain.
     */
    @ElementDefaultAttribute(
            value = "50012",
            description = "The port for the NEO blockchain."
    )
    String NEO_BLOCKCHAIN_PORT = "dev.getelements.elements.blockchain.neo.port";

    /**
     * Used to specify the file path for static content.
     */
    @ElementDefaultAttribute(
            value = "content",
            description = "The file path for static content storage."
    )
    String CDN_FILE_DIRECTORY = "dev.getelements.elements.cdnserve.storage.directory";

    /**
     * Used to specify the endpoint file path for cloning static content.
     */
    @ElementDefaultAttribute(
            value = "clone",
            description = "The endpoint for cloning static content."
    )
    String CDN_CLONE_ENDPOINT = "dev.getelements.elements.cdnserve.endpoint.clone";

    /**
     * Used to specify the endpoint for serving static content.
     */
    @ElementDefaultAttribute(
            value = "serve",
            description = "The endpoint for serving static content."
    )
    String CDN_SERVE_ENDPOINT = "dev.getelements.elements.cdnserve.endpoint.serve";

}
