package com.namazustudios.socialengine.model.application;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
public class IosApplicationConfiguration extends ApplicationConfiguration {

    private String applicationId;

    /**
     * Gets the Application ID, as defined in the AppStore (com.mycompany.app)
     * @return the app id
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID, as deinfed in the AppStore (com.mycompany.app)
     * @param applicationId
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

}
