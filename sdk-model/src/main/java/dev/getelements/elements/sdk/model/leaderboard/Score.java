package dev.getelements.elements.sdk.model.leaderboard;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

@Schema
public class Score {

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The ID of the Score")
    private String id;

    @NotNull
    private Profile profile;

    @Schema(description = "The point value of the score.")
    private double pointValue;

    @Null
    @Schema(description = "The the units of measure for the points.  For example, if the points in the game were called " +
                      "\"coins\" instead of \"points\" this would be used to designate as such in the UI.")
    private String scoreUnits;

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The time at which the score was created on the server.")
    private Long creationTimestamp;

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "The epoch to which the score belongs for the associated leaderboard. By convention, if the " +
            "leaderboard is all-time, this value will be set to zero.")
    private Long leaderboardEpoch;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public double getPointValue() {
        return pointValue;
    }

    public void setPointValue(double pointValue) {
        this.pointValue = pointValue;
    }

    public String getScoreUnits() {
        return scoreUnits;
    }

    public void setScoreUnits(String scoreUnits) {
        this.scoreUnits = scoreUnits;
    }

    public Long getCreationTimestamp() { return creationTimestamp; }

    public void setCreationTimestamp(Long creationTimestamp) { this.creationTimestamp = creationTimestamp; }

    public Long getLeaderboardEpoch() {
        return leaderboardEpoch;
    }

    public void setLeaderboardEpoch(Long leaderboardEpoch) {
        this.leaderboardEpoch = leaderboardEpoch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Score)) return false;

        Score score = (Score) o;

        if (Double.compare(score.getPointValue(), getPointValue()) != 0) return false;
        if (getId() != null ? !getId().equals(score.getId()) : score.getId() != null) return false;
        if (getProfile() != null ? !getProfile().equals(score.getProfile()) : score.getProfile() != null) return false;
        if (getCreationTimestamp() != null ? !getCreationTimestamp().equals(score.getCreationTimestamp()) : score.getCreationTimestamp() != null) return false;
        if (getLeaderboardEpoch() != null ? !getLeaderboardEpoch().equals(score.getLeaderboardEpoch()) : score.getLeaderboardEpoch() != null) return false;
        return getScoreUnits() != null ? getScoreUnits().equals(score.getScoreUnits()) : score.getScoreUnits() == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        temp = Double.doubleToLongBits(getPointValue());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getScoreUnits() != null ? getScoreUnits().hashCode() : 0);
        result = 31 * result + (getCreationTimestamp() != null ? getCreationTimestamp().hashCode() : 0);
        result = 31 * result + (getLeaderboardEpoch() != null ? getLeaderboardEpoch().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id='" + id + '\'' +
                ", profile=" + profile +
                ", pointValue=" + pointValue +
                ", scoreUnits='" + scoreUnits + '\'' +
                ", creationTimestamp='" + creationTimestamp + '\'' +
                ", leaderboardEpoch='" + leaderboardEpoch + '\'' +
                '}';
    }

}
