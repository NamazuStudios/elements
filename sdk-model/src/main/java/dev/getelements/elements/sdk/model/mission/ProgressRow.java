package dev.getelements.elements.sdk.model.mission;

import java.util.Objects;

/** Represents a single row in a mission progress listing, suitable for display. */
public class ProgressRow {

    /** Creates a new instance. */
    public ProgressRow() {}

    private String id;

    private String profileId;

    private String profileImageUrl;

    private String profileDisplayName;

    private String stepDisplayName;

    private String stepDescription;

    private int remaining;

    private int stepCount;

    /**
     * Returns the progress ID.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the progress ID.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the profile ID of the user.
     *
     * @return the profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profile ID of the user.
     *
     * @param profileId the profile ID
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * Returns the profile image URL of the user.
     *
     * @return the profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the profile image URL of the user.
     *
     * @param profileImageUrl the profile image URL
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Returns the profile display name of the user.
     *
     * @return the profile display name
     */
    public String getProfileDisplayName() {
        return profileDisplayName;
    }

    /**
     * Sets the profile display name of the user.
     *
     * @param profileDisplayName the profile display name
     */
    public void setProfileDisplayName(String profileDisplayName) {
        this.profileDisplayName = profileDisplayName;
    }

    /**
     * Returns the display name of the current step.
     *
     * @return the step display name
     */
    public String getStepDisplayName() {
        return stepDisplayName;
    }

    /**
     * Sets the display name of the current step.
     *
     * @param stepDisplayName the step display name
     */
    public void setStepDisplayName(String stepDisplayName) {
        this.stepDisplayName = stepDisplayName;
    }

    /**
     * Returns the description of the current step.
     *
     * @return the step description
     */
    public String getStepDescription() {
        return stepDescription;
    }

    /**
     * Sets the description of the current step.
     *
     * @param stepDescription the step description
     */
    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    /**
     * Returns the number of remaining actions to complete the current step.
     *
     * @return the remaining count
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * Sets the number of remaining actions to complete the current step.
     *
     * @param remaining the remaining count
     */
    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    /**
     * Returns the total number of steps in the mission.
     *
     * @return the step count
     */
    public int getStepCount() {
        return stepCount;
    }

    /**
     * Sets the total number of steps in the mission.
     *
     * @param stepCount the step count
     */
    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressRow that = (ProgressRow) o;
        return getRemaining() == that.getRemaining() && getStepCount() == that.getStepCount() && Objects.equals(getId(), that.getId()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getProfileImageUrl(), that.getProfileImageUrl()) && Objects.equals(getProfileDisplayName(), that.getProfileDisplayName()) && Objects.equals(getStepDisplayName(), that.getStepDisplayName()) && Objects.equals(getStepDescription(), that.getStepDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProfileId(), getProfileImageUrl(), getProfileDisplayName(), getStepDisplayName(), getStepDescription(), getRemaining(), getStepCount());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProgressRow{");
        sb.append("id='").append(id).append('\'');
        sb.append(", profileId='").append(profileId).append('\'');
        sb.append(", profileImageUrl='").append(profileImageUrl).append('\'');
        sb.append(", profileDisplayName='").append(profileDisplayName).append('\'');
        sb.append(", stepName='").append(stepDisplayName).append('\'');
        sb.append(", stepDescription='").append(stepDescription).append('\'');
        sb.append(", remaining=").append(remaining);
        sb.append(", stepCount=").append(stepCount);
        sb.append('}');
        return sb.toString();
    }

}
