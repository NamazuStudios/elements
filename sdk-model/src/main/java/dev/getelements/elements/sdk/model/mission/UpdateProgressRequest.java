package dev.getelements.elements.sdk.model.mission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;

import java.io.Serializable;
import java.util.Objects;

/** Represents a request to update the current progress of a mission. */
public class UpdateProgressRequest implements Serializable {

    /** Creates a new instance. */
    public UpdateProgressRequest() {}

    @Null
    @Schema(description = "The current step")
    private Step currentStep;

    @Null
    @Schema(description = "The remaining actions")
    private Integer remaining;

    /**
     * Returns the current step of the mission progress.
     *
     * @return the current step
     */
    public Step getCurrentStep() {
        return currentStep;
    }

    /**
     * Sets the current step of the mission progress.
     *
     * @param currentStep the current step
     */
    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Returns the number of remaining actions to complete the current step.
     *
     * @return the remaining count
     */
    public Integer getRemaining() {
        return remaining;
    }

    /**
     * Sets the number of remaining actions to complete the current step.
     *
     * @param remaining the remaining count
     */
    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UpdateProgressRequest that)) return false;
        return Objects.equals(currentStep, that.currentStep) && Objects.equals(remaining, that.remaining);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentStep, remaining);
    }

    @Override
    public String toString() {
        return "UpdateProgressRequest{" +
                "currentStep=" + currentStep +
                ", remaining=" + remaining +
                '}';
    }
}