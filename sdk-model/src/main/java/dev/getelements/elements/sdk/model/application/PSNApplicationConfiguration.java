package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.Constants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * Houses the necessary information for the Sony WebAPI to communicate with the SocialEngine system.
 *
 * Created by patricktwohig on 7/9/15.
 */
public class PSNApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String npIdentifier;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String clientSecret;

    /**
     * Corresponds to the SonyNP Identifier.
     *
     * @return the sony NP Identifier.
     */
    public String getNpIdentifier() {
        return npIdentifier;
    }

    /**
     * Sets the Sony NP Identififer.
     *
     * @param npIdentifier
     */
    public void setNpIdentifier(String npIdentifier) {
        this.npIdentifier = npIdentifier;
    }

    /**
     * Gets the ClientSecret, which is used to interact with the Sony WebAPI.
     *
     * @return the client Secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the ClientSecret, which is used to interact with the Sony WebAPI.
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSNApplicationConfiguration)) return false;
        if (!super.equals(o)) return false;

        PSNApplicationConfiguration that = (PSNApplicationConfiguration) o;

        if (getNpIdentifier() != null ? !getNpIdentifier().equals(that.getNpIdentifier()) : that.getNpIdentifier() != null)
            return false;
        return getClientSecret() != null ? getClientSecret().equals(that.getClientSecret()) : that.getClientSecret() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getNpIdentifier() != null ? getNpIdentifier().hashCode() : 0);
        result = 31 * result + (getClientSecret() != null ? getClientSecret().hashCode() : 0);
        return result;
    }

}
