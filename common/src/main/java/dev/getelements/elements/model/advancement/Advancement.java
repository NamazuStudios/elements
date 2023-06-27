package dev.getelements.elements.model.advancement;

import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.model.reward.Reward;
import dev.getelements.elements.model.mission.Step;

import java.util.List;
import java.util.Objects;

public class Advancement {

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
