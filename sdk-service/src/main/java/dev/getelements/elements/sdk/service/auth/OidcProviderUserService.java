package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.oidc.OidcProviderUserInfo;

/**
 * Resolves the claims served by the Elements OIDC provider's userinfo endpoint. Claims are gated by the
 * scopes recorded on the grant backing the presented access token: {@code sub} always, {@code name} and
 * {@code preferred_username} under {@code profile}, {@code email} and {@code email_verified} under
 * {@code email}.
 */
public interface OidcProviderUserService {

    /**
     * Verifies the supplied Elements session token (the {@code access_token} issued by this provider), loads
     * the grant recording the scopes delegated to its client, and builds the claim set.
     *
     * @param sessionToken the presented access token
     * @return the userinfo claims
     */
    OidcProviderUserInfo getUserInfo(String sessionToken);

}
