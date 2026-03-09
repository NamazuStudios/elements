package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

/** Represents a request to create a mission progress entry. */
public class CreateProgressRequest implements Serializable {

    /** Creates a new instance. */
    public CreateProgressRequest() {}

    @NotNull(groups={ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The profile of the owner of this progress")
    private Profile profile;

    @NotNull(groups={ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The mission")
    private ProgressMissionInfo mission;

    /**
     * Returns the profile of the progress owner.
     *
     * @return the profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Sets the profile of the progress owner.
     *
     * @param profile the profile
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /**
     * Returns the mission associated with this progress.
     *
     * @return the mission info
     */
    public ProgressMissionInfo getMission() {
        return mission;
    }

    /**
     * Sets the mission associated with this progress.
     *
     * @param mission the mission info
     */
    public void setMission(ProgressMissionInfo mission) {
        this.mission = mission;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateProgressRequest that)) return false;
        return Objects.equals(profile, that.profile) && Objects.equals(mission, that.mission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, mission);
    }

    @Override
    public String toString() {
        return "CreateProgressRequest{" +
                "profile=" + profile +
                ", mission=" + mission +
                '}';
    }
}