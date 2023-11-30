package dev.getelements.elements.model.application;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
public class GoogleSignInApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    private String applicationId;

    private Map<String, String> clientIds;

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

    public Map<String, String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(Map<String, String> clientIds) {
        this.clientIds = clientIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GooglePlayApplicationConfiguration that = (GooglePlayApplicationConfiguration) o;
        return Objects.equals(getApplicationId(), that.getApplicationId()) &&
                Objects.equals(getClientIds(), that.getJsonKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getApplicationId(), getClientIds());
    }

    @Override
    public String toString() {
        return "GooglePlayApplicationConfiguration{" +
                "applicationId='" + applicationId + '\'' +
                ", clientIds=" + clientIds +
                '}';
    }
}
