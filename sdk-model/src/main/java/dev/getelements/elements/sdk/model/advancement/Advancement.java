package dev.getelements.elements.sdk.model.advancement;

import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.reward.Reward;
import dev.getelements.elements.sdk.model.mission.Step;

import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a player advancing through a {@link Mission}, including the current
 * step reached, any steps completed, and any rewards earned during the advancement.
 */
public class Advancement {

    /** Creates a new instance. */
    public Advancement() {}

    private Step currentStep;

    private List<Step> completedSteps;

    private List<Reward> rewardsEarned;

    /**
     * The current {@link Step} after the advancement through the {@link Mission}.
     *
     * @return the step
     */
    public Step getCurrentStep() {
        return currentStep;
    }

    /**
     * Sets the current {@link Step} after the advancement through the {@link Mission}.
     *
     * @param currentStep the step
     */
    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Any steps that were completed as part of this {@link Advancement}.
     *
     * @return the list of steps
     */
    public List<Step> getCompletedSteps() {
        return completedSteps;
    }

    /**
     * Sets the steps that were completed as part of this {@link Advancement}.
     *
     * @param completedSteps the list of steps
     */
    public void setCompletedSteps(List<Step> completedSteps) {
        this.completedSteps = completedSteps;
    }

    /**
     * All {@link Reward} instances earned as part of this {@link Advancement}.
     *
     * @return ths list of rewards
     */
    public List<Reward> getRewardsEarned() {
        return rewardsEarned;
    }

    /**
     * Sets all {@link Reward} instances earned as part of this {@link Advancement}.
     *
     * @param rewardsEarned the list of rewards
     */
    public void setRewardsEarned(List<Reward> rewardsEarned) {
        this.rewardsEarned = rewardsEarned;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Advancement)) return false;
        Advancement that = (Advancement) object;
        return Objects.equals(getCurrentStep(), that.getCurrentStep()) &&
                Objects.equals(getCompletedSteps(), that.getCompletedSteps()) &&
                Objects.equals(getRewardsEarned(), that.getRewardsEarned());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrentStep(), getCompletedSteps(), getRewardsEarned());
    }

    @Override
    public String toString() {
        return "Advancement{" +
                "currentStep=" + currentStep +
                ", completedSteps=" + completedSteps +
                ", rewardsEarned=" + rewardsEarned +
                '}';
    }

}
