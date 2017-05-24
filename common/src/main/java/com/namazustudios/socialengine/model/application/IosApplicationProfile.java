package com.namazustudios.socialengine.model.application;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
public class IosApplicationProfile {

    private String appId;

    /**
     * Gets the Application ID, as defined in the AppStore (com.mycompany.app)
     * @return the app id
     */
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

}
