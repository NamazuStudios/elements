package dev.getelements.elements.service.auth.oidc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The subset of an OIDC/OAuth2 token endpoint response needed by the browser-redirect login flow. Internal
 * wire-format DTO only. {@code access_token}/{@code refresh_token} are intentionally not modeled here — provider
 * tokens are discarded immediately after the id_token is extracted and are never stored or returned to the client.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OidcTokenResponse {

    @JsonProperty("id_token")
    private String idToken;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

}
