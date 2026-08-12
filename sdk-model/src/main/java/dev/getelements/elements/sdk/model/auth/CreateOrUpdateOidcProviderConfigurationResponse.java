package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a response from creating or updating an {@link OidcProviderConfiguration}.
 *
 * <p>The service layer is responsible for clearing {@link OidcProviderConfiguration#getClientSecret()} on the
 * {@link #configuration} before returning it here — the client secret must never be echoed back to a caller.
 */
@Schema(description = "Represents a response from creating or updating an OIDC provider configuration. " +
        "The client secret is never included in the returned configuration.")
public class CreateOrUpdateOidcProviderConfigurationResponse {

    /** Creates a new instance. */
    public CreateOrUpdateOidcProviderConfigurationResponse() {}

    @Schema(description = "The created or updated provider configuration, with the client secret cleared.")
    private OidcProviderConfiguration configuration;

    @Schema(description = "The issuer resolved from the provider's discovery document, confirming discovery succeeded.")
    private String issuer;

    @Schema(description = "The authorization endpoint resolved from the provider's discovery document.")
    private String authorizationEndpoint;

    @Schema(description = "The token endpoint resolved from the provider's discovery document.")
    private String tokenEndpoint;

    /**
     * Returns the created or updated provider configuration.
     *
     * @return the configuration
     */
    public OidcProviderConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the created or updated provider configuration.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(OidcProviderConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the issuer resolved from the provider's discovery document.
     *
     * @return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Sets the issuer resolved from the provider's discovery document.
     *
     * @param issuer the issuer
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Returns the authorization endpoint resolved from the provider's discovery document.
     *
     * @return the authorization endpoint
     */
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    /**
     * Sets the authorization endpoint resolved from the provider's discovery document.
     *
     * @param authorizationEndpoint the authorization endpoint
     */
    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    /**
     * Returns the token endpoint resolved from the provider's discovery document.
     *
     * @return the token endpoint
     */
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    /**
     * Sets the token endpoint resolved from the provider's discovery document.
     *
     * @param tokenEndpoint the token endpoint
     */
    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

}
