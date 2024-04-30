package dev.getelements.elements.model.mission;

import java.util.Objects;

public class ProgressRow {

    private String id;

    private String profileId;

    private String profileImageUrl;

    private String profileDisplayName;

    private String stepDisplayName;

    private String stepDescription;

    private int remaining;

    private int stepCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileDisplayName() {
        return profileDisplayName;
    }

    public void setProfileDisplayName(String profileDisplayName) {
        this.profileDisplayName = profileDisplayName;
    }

    public String getStepDisplayName() {
        return stepDisplayName;
    }

    public void setStepDisplayName(String stepDisplayName) {
        this.stepDisplayName = stepDisplayName;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public int getStepCount() {
        return stepCount;
    }

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
