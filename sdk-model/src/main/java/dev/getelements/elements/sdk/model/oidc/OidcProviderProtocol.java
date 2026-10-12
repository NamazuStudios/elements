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

    /** The only PKCE challenge method supported by the provider. */
    public static final String CODE_CHALLENGE_METHOD_S256 = "S256";

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
