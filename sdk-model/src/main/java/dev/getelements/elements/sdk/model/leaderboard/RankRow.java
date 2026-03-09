package dev.getelements.elements.sdk.model.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.Objects;

/** Represents a single row in a leaderboard ranking result set. */
@Schema
public class RankRow {

    /** Creates a new instance. */
    public RankRow() {}

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

    /**
     * Returns the ID of the score.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the score.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the position of the score in the result set.
     *
     * @return the position
     */
    public long getPosition() {
        return position;
    }

    /**
     * Sets the position of the score in the result set.
     *
     * @param position the position
     */
    public void setPosition(long position) {
        this.position = position;
    }

    /**
     * Returns the point value of the score.
     *
     * @return the point value
     */
    public double getPointValue() {
        return pointValue;
    }

    /**
     * Sets the point value of the score.
     *
     * @param pointValue the point value
     */
    public void setPointValue(double pointValue) {
        this.pointValue = pointValue;
    }

    /**
     * Returns the units of measure for the score.
     *
     * @return the score units
     */
    public String getScoreUnits() {
        return scoreUnits;
    }

    /**
     * Sets the units of measure for the score.
     *
     * @param scoreUnits the score units
     */
    public void setScoreUnits(String scoreUnits) {
        this.scoreUnits = scoreUnits;
    }

    /**
     * Returns the timestamp at which the score was created on the server.
     *
     * @return the creation timestamp
     */
    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * Sets the timestamp at which the score was created on the server.
     *
     * @param creationTimestamp the creation timestamp
     */
    public void setCreationTimestamp(Long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    /**
     * Returns the leaderboard epoch to which the score belongs.
     *
     * @return the leaderboard epoch
     */
    public Long getLeaderboardEpoch() {
        return leaderboardEpoch;
    }

    /**
     * Sets the leaderboard epoch to which the score belongs.
     *
     * @param leaderboardEpoch the leaderboard epoch
     */
    public void setLeaderboardEpoch(Long leaderboardEpoch) {
        this.leaderboardEpoch = leaderboardEpoch;
    }

    /**
     * Returns the profile ID of the user who holds this rank.
     *
     * @return the profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profile ID of the user who holds this rank.
     *
     * @param profileId the profile ID
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * Returns the profile display name of the user who holds this rank.
     *
     * @return the profile display name
     */
    public String getProfileDisplayName() {
        return profileDisplayName;
    }

    /**
     * Sets the profile display name of the user who holds this rank.
     *
     * @param profileDisplayName the profile display name
     */
    public void setProfileDisplayName(String profileDisplayName) {
        this.profileDisplayName = profileDisplayName;
    }

    /**
     * Returns the profile image URL of the user who holds this rank.
     *
     * @return the profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the profile image URL of the user who holds this rank.
     *
     * @param profileImageUrl the profile image URL
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Returns the last login time of the user.
     *
     * @return the last login
     */
    public long getLastLogin() {
        return lastLogin;
    }

    /**
     * Sets the last login time of the user.
     *
     * @param lastLogin the last login
     */
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
