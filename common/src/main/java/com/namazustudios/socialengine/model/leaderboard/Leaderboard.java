package com.namazustudios.socialengine.model.leaderboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.namazustudios.socialengine.model.ValidationGroups.Update;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ApiModel
public class Leaderboard {

    @Null
    private String id;

    @NotNull
    @ApiModelProperty("The unique-name of the leaderboard.  This must be unique across all leaderboards.")
    private String name;

    @NotNull
    @ApiModelProperty("The user-presentable name or title for for the leaderboard.")
    private String title;

    @NotNull
    @ApiModelProperty("The units-of measure for the score type of the leaderboard.")
    private String scoreUnits;

    @Null(groups = {Update.class})
    @ApiModelProperty("The time at which the leaderboard intervals should begin (in ms).")
    private Long dateStart;

    @Null(groups = {Update.class})
    @ApiModelProperty("The duration for a leaderboard interval (in ms).")
    private Long interval;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScoreUnits() {
        return scoreUnits;
    }

    public void setScoreUnits(String scoreUnits) {
        this.scoreUnits = scoreUnits;
    }

    public Long getDateStart() { return dateStart; }

    public void setDateStart(Long dateStart) { this.dateStart = dateStart; }

    public Long getInterval() { return interval; }

    public void setInterval(Long interval) { this.interval = interval; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leaderboard)) return false;

        Leaderboard that = (Leaderboard) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;
        if (getDateStart() != null ? !getDateStart().equals(that.getDateStart()) : that.getDateStart() != null) return false;
        if (getInterval() != null ? !getInterval().equals(that.getInterval()) : that.getInterval() != null) return false;
        return getScoreUnits() != null ? getScoreUnits().equals(that.getScoreUnits()) : that.getScoreUnits() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getScoreUnits() != null ? getScoreUnits().hashCode() : 0);
        result = 31 * result + (getDateStart() != null ? getDateStart().hashCode() : 0);
        result = 31 * result + (getInterval() != null ? getInterval().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Leaderboard{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", scoreUnits='" + scoreUnits + '\'' +
                ", dateStart='" + dateStart + '\'' +
                ", interval='" + interval + '\'' +
                '}';
    }

}
