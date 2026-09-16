package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/** Represents a request to create or update the system-wide {@link CaptchaConfiguration}. */
@Schema(description = "Represents a request to create or update the system-wide CAPTCHA configuration.")
public class CreateOrUpdateCaptchaConfigurationRequest {

    /** Creates a new instance. */
    public CreateOrUpdateCaptchaConfigurationRequest() {}

    @NotNull
    @Schema(description = "The CAPTCHA provider backing this configuration.")
    private CaptchaProvider provider;

    @NotBlank
    @Schema(description = "The provider's public site key.")
    private String siteKey;

    @Schema(description = "The provider's private secret key. Required the first time a configuration is " +
            "created; on subsequent updates, leave blank to keep the existing secret unchanged.")
    private String secretKey;

    @Schema(description = "Whether CAPTCHA verification should be enforced.")
    private boolean enabled;

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

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateOrUpdateCaptchaConfigurationRequest that = (CreateOrUpdateCaptchaConfigurationRequest) o;
        return enabled == that.enabled
                && provider == that.provider
                && Objects.equals(siteKey, that.siteKey)
                && Objects.equals(secretKey, that.secretKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, siteKey, secretKey, enabled);
    }

    @Override
    public String toString() {
        return "CreateOrUpdateCaptchaConfigurationRequest{" +
                "provider=" + provider +
                ", siteKey='" + siteKey + '\'' +
                ", enabled=" + enabled +
                '}';
    }

}
