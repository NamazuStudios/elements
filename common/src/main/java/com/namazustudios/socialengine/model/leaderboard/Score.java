package com.namazustudios.socialengine.model.leaderboard;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ApiModel
public class Score {

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The ID of the Score")
    private String id;

    @NotNull
    private Profile profile;

    @ApiModelProperty("The point value of the score.")
    private double pointValue;

    @Null
    @ApiModelProperty("The the units of measure for the points.  For example, if the points in the game were called " +
                      "\"coins\" instead of \"points\" this would be used to designate as such in the UI.")
    private String scoreUnits;

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Insert.class)
    @ApiModelProperty("The time at which the score was created on the server.")
    private Long creationDate;

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

    public Long getCreationDate() { return creationDate; }

    public void setCreationDate(Long creationDate) { this.creationDate = creationDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Score)) return false;

        Score score = (Score) o;

        if (Double.compare(score.getPointValue(), getPointValue()) != 0) return false;
        if (getId() != null ? !getId().equals(score.getId()) : score.getId() != null) return false;
        if (getProfile() != null ? !getProfile().equals(score.getProfile()) : score.getProfile() != null) return false;
        if (getCreationDate() != null ? !getCreationDate().equals(score.getCreationDate()) : score.getCreationDate() != null) return false;
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
        result = 31 * result + (getCreationDate() != null ? getCreationDate().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id='" + id + '\'' +
                ", profile=" + profile +
                ", pointValue=" + pointValue +
                ", scoreUnits='" + scoreUnits + '\'' +
                ", creationDate='" + creationDate + '\'' +
                '}';
    }

}
