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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GooglePlayApplicationConfiguration)) return false;
        if (!super.equals(o)) return false;

        GooglePlayApplicationConfiguration that = (GooglePlayApplicationConfiguration) o;

        return getApplicationId() != null ? getApplicationId().equals(that.getApplicationId()) : that.getApplicationId() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getApplicationId() != null ? getApplicationId().hashCode() : 0);
        return result;
    }

}
