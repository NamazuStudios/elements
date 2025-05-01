package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
@Schema(description = "Configuration for the iOS Application Configuration")
public class IosApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    @NotNull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IosApplicationConfiguration that = (IosApplicationConfiguration) o;
        return Objects.equals(getApplicationId(), that.getApplicationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getApplicationId());
    }

    @Override
    public String toString() {
        return "IosApplicationConfiguration{" +
                "applicationId='" + applicationId + '\'' +
                '}';
    }

}
