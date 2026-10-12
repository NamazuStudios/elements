package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderTokenResponse;

/**
 * The Elements OIDC provider's token endpoint: redeems authorization codes and refresh tokens for token
 * responses.
 *
 * <p>The {@code access_token} in every response is an Elements session token minted through the shared
 * {@link SessionTokenIssuer}, so a downstream application may call Elements APIs with it directly. The
 * {@code id_token} is signed with the same signing-key lifecycle, carrying {@code iss}, {@code sub},
 * {@code aud}, {@code azp}, {@code exp}, {@code iat}, {@code auth_time}, and (when the authorize request
 * supplied one) {@code nonce}.</p>
 */
public interface OidcProviderTokenService {

    /**
     * Authenticates the client, redeems the single-use authorization code, and issues the token response
     * (access token, id_token, and — when the registration allows the {@code refresh_token} grant — a
     * refresh token).
     *
     * @param request the token request parameters
     * @return the token response
     */
    OidcProviderTokenResponse exchangeCode(OidcProviderTokenRequest request);

    /**
     * Authenticates the client, consumes the single-use refresh token, rotates the grant's backing session,
     * and issues a fresh token response (new access token, new id_token, new refresh token). A replayed
     * refresh token fails closed.
     *
     * @param request the token request parameters
     * @return the token response
     */
    OidcProviderTokenResponse refresh(OidcProviderTokenRequest request);

    /**
     * Ends the session(s) identified by the supplied {@code id_token_hint}: verifies the id_token's signature
     * against this deployment's signing keys, then for every grant the identified user holds with the
     * identified client ({@code sub} + {@code azp}), revokes the backing Elements session, deletes the
     * grant, and revokes every refresh token. Tokens already issued to the downstream application stop
     * verifying immediately (their backing session is gone), satisfying the strictest logout-propagation
     * position.
     *
     * @param idTokenHint the id_token to end
     */
    void endSession(String idTokenHint);

}
