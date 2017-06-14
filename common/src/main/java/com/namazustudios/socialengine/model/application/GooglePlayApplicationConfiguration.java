package com.namazustudios.socialengine.model.application;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
public class GooglePlayApplicationConfiguration extends ApplicationConfiguration {

    private String applicationId;

    /**
     * Gets the Application ID, as defined in Google Play (com.mycompany.app)
     *
     * @return
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the Application ID
     *
     * @param applicationId
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

}
