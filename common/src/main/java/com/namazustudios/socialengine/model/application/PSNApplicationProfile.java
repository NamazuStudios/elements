package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Houses the necessary information for the Sony WebAPI to communicate with the SocialEngine system.
 *
 * Created by patricktwohig on 7/9/15.
 */
public class PSNApplicationProfile extends ApplicationProfile {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
    private String npIdentifier;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NON_BLANK_STRING)
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
     * @return the Client SEcret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the ClientSecret, which is used to interact with the Sony WebAPI.
     *
     * @return the Client SEcret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}
