package dev.getelements.elements.model.session;


import dev.getelements.elements.model.application.Application;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;

@ApiModel(description = "Used to create a mock session with the server.  This will create a temporary user as well " +
                        "which will exist for a short period of time")
public class MockSessionRequest {

    @Min(60)
    @ApiModelProperty("The lifetime of the user in seconds.  After this amount of time, ")
    private Integer lifetimeInSeconds;

    @ApiModelProperty("The Application to use when creating the associated user profile.  If null, then no profile " +
                      "will be generated.")
    private Application application;

    public Integer getLifetimeInSeconds() {
        return lifetimeInSeconds;
    }

    public void setLifetimeInSeconds(Integer lifetimeInSeconds) {
        this.lifetimeInSeconds = lifetimeInSeconds;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MockSessionRequest)) return false;

        MockSessionRequest that = (MockSessionRequest) o;

        if (getLifetimeInSeconds() != null ? !getLifetimeInSeconds().equals(that.getLifetimeInSeconds()) : that.getLifetimeInSeconds() != null)
            return false;
        return getApplication() != null ? getApplication().equals(that.getApplication()) : that.getApplication() == null;
    }

    @Override
    public int hashCode() {
        int result = getLifetimeInSeconds() != null ? getLifetimeInSeconds().hashCode() : 0;
        result = 31 * result + (getApplication() != null ? getApplication().hashCode() : 0);
        return result;
    }

}
