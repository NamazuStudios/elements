package dev.getelements.elements.exception;

import dev.getelements.elements.model.health.HealthStatus;

public class UnhealthyException extends InternalException {

    private final HealthStatus healthStatus;

    public UnhealthyException(final HealthStatus healthStatus) {
        super("Instance is unhealthy.");
        this.healthStatus = healthStatus;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.UNHEALTHY;
    }

}
