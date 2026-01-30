package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class CreateProgressRequest implements Serializable {

    @NotNull(groups={ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The profile of the owner of this progress")
    private Profile profile;

    @NotNull(groups={ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The mission")
    private ProgressMissionInfo mission;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public ProgressMissionInfo getMission() {
        return mission;
    }

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