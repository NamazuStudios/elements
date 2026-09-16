package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * Represents the system-wide CAPTCHA configuration. Unlike {@link OidcProviderConfiguration}, this is a single
 * global configuration rather than a named collection: only one CAPTCHA provider is active at a time.
 */
@Schema(description = "The system-wide CAPTCHA configuration.")
public class CaptchaConfiguration {

    /** Creates a new instance. */
    public CaptchaConfiguration() {}

    @Schema(description = "The unique ID of the configuration.")
    private String id;

    @Schema(description = "The CAPTCHA provider backing this configuration.")
    private CaptchaProvider provider = CaptchaProvider.RECAPTCHA;

    @Schema(description = "The provider's public site key. Safe to expose to any client; rendered by the " +
            "widget on the login page before any session exists.")
    private String siteKey;

    @Schema(description = "The provider's private secret key, used to verify responses server-side. Never " +
            "returned in API responses.")
    private String secretKey;

    @Schema(description = "Whether CAPTCHA verification is currently enforced.")
    private boolean enabled;

    /**
     * Returns the unique ID of the configuration.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the configuration.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the CAPTCHA provider.
     *
     * @return the provider
     */
    public CaptchaProvider getProvider() {
        return provider;
    }

    /**
     * Sets the CAPTCHA provider.
     *
     * @param provider the provider
     */
    public void setProvider(CaptchaProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns the provider's public site key.
     *
     * @return the site key
     */
    public String getSiteKey() {
        return siteKey;
    }

    /**
     * Sets the provider's public site key.
     *
     * @param siteKey the site key
     */
    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    /**
     * Returns the provider's private secret key.
     *
     * @return the secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Sets the provider's private secret key.
     *
     * @param secretKey the secret key
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Returns whether CAPTCHA verification is currently enforced.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether CAPTCHA verification is currently enforced.
     *
     * @param enabled true if enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptchaConfiguration that = (CaptchaConfiguration) o;
        return enabled == that.enabled
                && Objects.equals(id, that.id)
                && provider == that.provider
                && Objects.equals(siteKey, that.siteKey)
                && Objects.equals(secretKey, that.secretKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, siteKey, secretKey, enabled);
    }

    @Override
    public String toString() {
        return "CaptchaConfiguration{" +
                "id='" + id + '\'' +
                ", provider=" + provider +
                ", siteKey='" + siteKey + '\'' +
                ", enabled=" + enabled +
                '}';
    }

}
