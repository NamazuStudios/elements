package dev.getelements.elements.sdk.model.health;

import dev.getelements.elements.sdk.model.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/** Represents an error response that includes health status information. */
@Schema
public class HealthErrorResponse extends ErrorResponse {

    /** Creates a new instance. */
    public HealthErrorResponse() {}

    @Schema
    private HealthStatus healthStatus;

    /**
     * Returns the health status associated with the error.
     *
     * @return the health status
     */
    @Schema
    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    /**
     * Sets the health status associated with the error.
     *
     * @param healthStatus the health status
     */
    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HealthErrorResponse that = (HealthErrorResponse) o;
        return Objects.equals(healthStatus, that.healthStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), healthStatus);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthErrorResponse{");
        sb.append("healthStatus=").append(healthStatus);
        sb.append('}');
        return sb.toString();
    }


}
