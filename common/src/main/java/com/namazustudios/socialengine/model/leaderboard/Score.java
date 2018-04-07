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

    @NotNull
    @ApiModelProperty("The the units of measure for the points.  For example, if the points in the game were called " +
                      "\"coins\" instead of \"points\" this would be used to designate as such in the UI.")
    private String pointUnits;

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

    public String getPointUnits() {
        return pointUnits;
    }

    public void setPointUnits(String pointUnits) {
        this.pointUnits = pointUnits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Score)) return false;

        Score score = (Score) o;

        if (Double.compare(score.getPointValue(), getPointValue()) != 0) return false;
        if (getId() != null ? !getId().equals(score.getId()) : score.getId() != null) return false;
        if (getProfile() != null ? !getProfile().equals(score.getProfile()) : score.getProfile() != null) return false;
        return getPointUnits() != null ? getPointUnits().equals(score.getPointUnits()) : score.getPointUnits() == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        temp = Double.doubleToLongBits(getPointValue());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getPointUnits() != null ? getPointUnits().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id='" + id + '\'' +
                ", profile=" + profile +
                ", pointValue=" + pointValue +
                ", pointUnits='" + pointUnits + '\'' +
                '}';
    }

}
