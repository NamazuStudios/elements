package dev.getelements.elements.sdk.model.mission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;

import java.io.Serializable;
import java.util.Objects;

public class UpdateProgressRequest implements Serializable {

    @Null
    @Schema(description = "The current step")
    private Step currentStep;

    @Null
    @Schema(description = "The remaining actions")
    private Integer remaining;

    public Step getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getRemaining() {
        return remaining;
    }

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