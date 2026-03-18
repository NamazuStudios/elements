package dev.getelements.elements.sdk.model.exception;

import dev.getelements.elements.sdk.model.health.HealthStatus;

/** Thrown when the service instance is in an unhealthy state. */
public class UnhealthyException extends InternalException {

    private final HealthStatus healthStatus;

    /**
     * Creates a new instance with the given health status.
     * @param healthStatus the current health status
     */
    public UnhealthyException(final HealthStatus healthStatus) {
        super("Instance is unhealthy.");
        this.healthStatus = healthStatus;
    }

    /**
     * Returns the current health status.
     * @return the health status
     */
    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.UNHEALTHY;
    }

}
