package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * Created by patricktwohig on 6/14/17.
 */
@Schema(
    description = "Houses the various parameters required which allow communication with " +
                  "the Faceook API.  The Facebook API will ")
public class FacebookApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    @NotNull
    @Schema(description = "The AppID as it appears in the Facebook Developer Console")
    private String applicationId;

    @NotNull
    @Schema(description = "The App Secret as it appears in the Facebook Developer Console")
    private String applicationSecret;

    @Schema(description = "The set of built-in permissions connected clients will need to request.")
    private List<String> builtinApplicationPermissions;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public List<String> getBuiltinApplicationPermissions() {
        return builtinApplicationPermissions;
    }

    public void setBuiltinApplicationPermissions(List<String> builtinApplicationPermissions) {
        this.builtinApplicationPermissions = builtinApplicationPermissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookApplicationConfiguration)) return false;
        if (!super.equals(o)) return false;

        FacebookApplicationConfiguration that = (FacebookApplicationConfiguration) o;

        if (getApplicationId() != null ? !getApplicationId().equals(that.getApplicationId()) : that.getApplicationId() != null)
            return false;
        if (getApplicationSecret() != null ? !getApplicationSecret().equals(that.getApplicationSecret()) : that.getApplicationSecret() != null)
            return false;
        return getBuiltinApplicationPermissions() != null ? getBuiltinApplicationPermissions().equals(that.getBuiltinApplicationPermissions()) : that.getBuiltinApplicationPermissions() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getApplicationId() != null ? getApplicationId().hashCode() : 0);
        result = 31 * result + (getApplicationSecret() != null ? getApplicationSecret().hashCode() : 0);
        result = 31 * result + (getBuiltinApplicationPermissions() != null ? getBuiltinApplicationPermissions().hashCode() : 0);
        return result;
    }

}
