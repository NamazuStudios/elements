package dev.getelements.elements.sdk.model.oidc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * The query parameters of an {@code /oidc/authorize} request, deserialized for service-layer validation.
 * Only the {@code code} response type is supported.
 */
@Schema(description = "The parameters of an OIDC provider authorization request.")
public class OidcProviderAuthorizationRequest {

    @Schema(description = "The response type. Only \"code\" is supported.")
    @NotBlank
    private String responseType;

    @Schema(description = "The client identifier of the registered relying party.")
    @NotBlank
    private String clientId;

    @Schema(description = "The redirect URI. Must exactly match a URI registered on the client.")
    @NotBlank
    private String redirectUri;

    @Schema(description = "The requested scope string, e.g. \"openid profile email\".")
    @NotBlank
    private String scope;

    @Schema(description = "The client-generated CSRF value, echoed back on redirect.")
    private String state;

    @Schema(description = "The client-generated nonce, embedded in the resulting id_token.")
    private String nonce;

    @Schema(description = "The PKCE code challenge. Required for all clients.")
    @NotBlank
    private String codeChallenge;

    @Schema(description = "The PKCE challenge method. Only \"S256\" is supported.")
    @NotBlank
    private String codeChallengeMethod;

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

}
