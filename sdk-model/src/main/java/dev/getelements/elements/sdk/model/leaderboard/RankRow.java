package dev.getelements.elements.sdk.model.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.Objects;

@Schema
public class RankRow {

    @Schema(description = "The ID of the Score")
    private String id;

    @Schema(description = "The position of the associated score in the result set.")
    private long position;

    @Schema(description = "The point value of the score.")
    private double pointValue;

    @Null
    @Schema(description = "The the units of measure for the points.  For example, if the points in the game were called " +
            "\"coins\" instead of \"points\" this would be used to designate as such in the UI.")
    private String scoreUnits;

    @Schema(description = "The time at which the score was created on the server.")
    private Long creationTimestamp;

    @Schema(description = "The epoch to which the score belongs for the associated leaderboard. By convention, if the " +
            "leaderboard is all-time, this value will be set to zero.")
    private Long leaderboardEpoch;

    @NotNull
    @Schema(description = "The profile ID of the user who holds this rank.")
    private String profileId;

    @NotNull
    @Schema(description = "The profile display name of the user who holds this rank.")
    private String profileDisplayName;

    @NotNull
    @Schema(description = "The profile image url of the user who holds this rank.")
    private String profileImageUrl;

    @NotNull
    @Schema(description = "The last login.")
    private long lastLogin;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
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

    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Long getLeaderboardEpoch() {
        return leaderboardEpoch;
    }

    public void setLeaderboardEpoch(Long leaderboardEpoch) {
        this.leaderboardEpoch = leaderboardEpoch;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileDisplayName() {
        return profileDisplayName;
    }

    public void setProfileDisplayName(String profileDisplayName) {
        this.profileDisplayName = profileDisplayName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankRow rankRow = (RankRow) o;
        return getPosition() == rankRow.getPosition() && Double.compare(getPointValue(), rankRow.getPointValue()) == 0 && getLastLogin() == rankRow.getLastLogin() && Objects.equals(getId(), rankRow.getId()) && Objects.equals(getScoreUnits(), rankRow.getScoreUnits()) && Objects.equals(getCreationTimestamp(), rankRow.getCreationTimestamp()) && Objects.equals(getLeaderboardEpoch(), rankRow.getLeaderboardEpoch()) && Objects.equals(getProfileId(), rankRow.getProfileId()) && Objects.equals(getProfileDisplayName(), rankRow.getProfileDisplayName()) && Objects.equals(getProfileImageUrl(), rankRow.getProfileImageUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPosition(), getPointValue(), getScoreUnits(), getCreationTimestamp(), getLeaderboardEpoch(), getProfileId(), getProfileDisplayName(), getProfileImageUrl(), getLastLogin());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RankRow{");
        sb.append("id='").append(id).append('\'');
        sb.append(", position=").append(position);
        sb.append(", pointValue=").append(pointValue);
        sb.append(", scoreUnits='").append(scoreUnits).append('\'');
        sb.append(", creationTimestamp=").append(creationTimestamp);
        sb.append(", leaderboardEpoch=").append(leaderboardEpoch);
        sb.append(", profileId='").append(profileId).append('\'');
        sb.append(", profileDisplayName='").append(profileDisplayName).append('\'');
        sb.append(", profileImageUrl='").append(profileImageUrl).append('\'');
        sb.append(", lastLogin=").append(lastLogin);
        sb.append('}');
        return sb.toString();
    }
}
