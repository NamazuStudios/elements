package com.namazustudios.socialengine.model.application;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
public class GooglePlayApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    private String applicationId;

    private Map<String, Object> jsonKey;

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

    public Map<String, Object> getJsonKey() {
        return jsonKey;
    }

    public void setJsonKey(Map<String, Object> jsonKey) {
        this.jsonKey = jsonKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GooglePlayApplicationConfiguration that = (GooglePlayApplicationConfiguration) o;
        return Objects.equals(getApplicationId(), that.getApplicationId()) &&
                Objects.equals(getJsonKey(), that.getJsonKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getApplicationId(), getJsonKey());
    }

    @Override
    public String toString() {
        return "GooglePlayApplicationConfiguration{" +
                "applicationId='" + applicationId + '\'' +
                ", jsonKey=" + jsonKey +
                '}';
    }
}
