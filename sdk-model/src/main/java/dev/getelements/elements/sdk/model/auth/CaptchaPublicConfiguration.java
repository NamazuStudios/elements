package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * Public, unauthenticated projection of {@link CaptchaConfiguration} used by any frontend (admin panel, game
 * clients, other panels) to decide whether to render a CAPTCHA widget and, if so, which provider and site key
 * to use. Never includes the secret key.
 */
@Schema(description = "Public bootstrap information describing the active CAPTCHA configuration, if any.")
public class CaptchaPublicConfiguration {

    /** Creates a new instance. */
    public CaptchaPublicConfiguration() {}

    @Schema(description = "Whether CAPTCHA verification is currently enforced.")
    private boolean enabled;

    @Schema(description = "The active CAPTCHA provider. Only meaningful when enabled is true.")
    private CaptchaProvider provider;

    @Schema(description = "The provider's public site key, used to render the CAPTCHA widget. Only present " +
            "when enabled is true.")
    private String siteKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CaptchaProvider getProvider() {
        return provider;
    }

    public void setProvider(CaptchaProvider provider) {
        this.provider = provider;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptchaPublicConfiguration that = (CaptchaPublicConfiguration) o;
        return enabled == that.enabled && provider == that.provider && Objects.equals(siteKey, that.siteKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, provider, siteKey);
    }

    @Override
    public String toString() {
        return "CaptchaPublicConfiguration{" +
                "enabled=" + enabled +
                ", provider=" + provider +
                ", siteKey='" + siteKey + '\'' +
                '}';
    }

}
