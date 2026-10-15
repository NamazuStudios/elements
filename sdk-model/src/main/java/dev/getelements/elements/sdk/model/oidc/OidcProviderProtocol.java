package dev.getelements.elements.sdk.model.oidc;

/**
 * Protocol constants for the Elements OIDC provider, kept alongside the provider DTOs so the authorization,
 * token, and userinfo services cannot drift from each other.
 */
public final class OidcProviderProtocol {

    /** The only response type supported by the provider. */
    public static final String RESPONSE_TYPE_CODE = "code";

    /** The authorization code grant. */
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    /** The refresh token grant. */
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    /** The client credentials grant, for machine clients with no end-user. */
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    /** The only PKCE challenge method supported by the provider. */
    public static final String CODE_CHALLENGE_METHOD_S256 = "S256";

    //
    // RFC 6749 error codes
    //

    /** The request is missing a required parameter or is otherwise malformed. */
    public static final String ERROR_INVALID_REQUEST = "invalid_request";

    /** Client authentication failed, or the client is unknown. */
    public static final String ERROR_INVALID_CLIENT = "invalid_client";

    /** The provided authorization grant or refresh token is invalid, expired, or replayed. */
    public static final String ERROR_INVALID_GRANT = "invalid_grant";

    /** The authenticated client is not authorized to use this grant type. */
    public static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";

    /** The authorization server does not support the requested grant type. */
    public static final String ERROR_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";

    /** The authorization server does not support the requested response type. */
    public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";

    /** The requested scope is invalid, unknown, or exceeds what the client may request. */
    public static final String ERROR_INVALID_SCOPE = "invalid_scope";

    /** The requested resource/audience is invalid or not permitted for this client. */
    public static final String ERROR_INVALID_TARGET = "invalid_target";

    /** The resource owner denied the request. */
    public static final String ERROR_ACCESS_DENIED = "access_denied";

    /** The authorization server encountered an unexpected condition. */
    public static final String ERROR_SERVER_ERROR = "server_error";

    /** Mandatory scope: presence gates every authorization. */
    public static final String SCOPE_OPENID = "openid";

    /** Gates the name and preferred_username userinfo claims. */
    public static final String SCOPE_PROFILE = "profile";

    /** Gates the email and email_verified userinfo claims. */
    public static final String SCOPE_EMAIL = "email";

    /** id_token claim: the party the token was issued to. */
    public static final String CLAIM_AZP = "azp";

    /** id_token claim: when the end-user authenticated, seconds since the epoch. */
    public static final String CLAIM_AUTH_TIME = "auth_time";

    private OidcProviderProtocol() {}

}
