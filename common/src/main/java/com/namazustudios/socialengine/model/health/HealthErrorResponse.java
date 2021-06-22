package com.namazustudios.socialengine.model.health;

import com.namazustudios.socialengine.model.ErrorResponse;

import java.util.Objects;

public class HealthErrorResponse extends ErrorResponse {

    private HealthStatus healthStatus;

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

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
