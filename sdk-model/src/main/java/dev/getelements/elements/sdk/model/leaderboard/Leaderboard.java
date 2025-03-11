package dev.getelements.elements.sdk.model.leaderboard;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

@Schema
public class Leaderboard {

    @Null
    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    @Schema(description = "The name of the leaderboard.  This must be unique across all leaderboards.")
    private String name;

    @NotNull
    @Schema(description = "The time strategy for the leaderboard. Current options are ALL_TIME and EPOCHAL.")
    private TimeStrategyType timeStrategyType;

    @NotNull
    @Schema(description = "The score strategy for the leaderboard. Current options are OVERWRITE_IF_GREATER and ACCUMULATE.")
    private ScoreStrategyType scoreStrategyType;

    @NotNull
    @Schema(description = "The user-presentable name or title for for the leaderboard.")
    private String title;

    @NotNull
    @Schema(description = "The units-of measure for the score type of the leaderboard.")
    private String scoreUnits;

    @Null(groups = {Update.class})
    @Min(0)
    @Schema(description = "The time at which the leaderboard epoch intervals should begin (in ms). If null, then " +
                        "the leaderboard is all-time and not epochal. During creation, if this value is provided, then " +
            "epochInterval must also be provided.")
    private Long firstEpochTimestamp;

    @Null(groups = {Update.class})
    @Min(0)
    @Schema(description = "The duration for a leaderboard epoch interval (in ms). If null, then " +
            "the leaderboard is all-time and not epochal. During creation, if this value is provided, then " +
            "firstEpochTimestamp must also be provided.")
    private Long epochInterval;

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

    public TimeStrategyType getTimeStrategyType() {
        return timeStrategyType;
    }

    public void setTimeStrategyType(TimeStrategyType timeStrategyType) {
        this.timeStrategyType = timeStrategyType;
    }

    public ScoreStrategyType getScoreStrategyType() {
        return scoreStrategyType;
    }

    public void setScoreStrategyType(ScoreStrategyType scoreStrategyType) {
        this.scoreStrategyType = scoreStrategyType;
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

    public Long getFirstEpochTimestamp() { return firstEpochTimestamp; }

    public void setFirstEpochTimestamp(Long firstEpochTimestamp) { this.firstEpochTimestamp = firstEpochTimestamp; }

    public Long getEpochInterval() { return epochInterval; }

    public void setEpochInterval(Long epochInterval) { this.epochInterval = epochInterval; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Leaderboard that = (Leaderboard) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                getTimeStrategyType() == that.getTimeStrategyType() &&
                getScoreStrategyType() == that.getScoreStrategyType() &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getScoreUnits(), that.getScoreUnits()) &&
                Objects.equals(getFirstEpochTimestamp(), that.getFirstEpochTimestamp()) &&
                Objects.equals(getEpochInterval(), that.getEpochInterval());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getTimeStrategyType(), getScoreStrategyType(), getTitle(),
                getScoreUnits(), getFirstEpochTimestamp(), getEpochInterval());
    }

    @Override
    public String toString() {
        return "Leaderboard{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", timeStrategyType=" + timeStrategyType +
                ", scoreStrategyType=" + scoreStrategyType +
                ", title='" + title + '\'' +
                ", scoreUnits='" + scoreUnits + '\'' +
                ", firstEpochTimestamp=" + firstEpochTimestamp +
                ", epochInterval=" + epochInterval +
                '}';
    }

    public enum TimeStrategyType {

        /**
         * The leaderboard continues without resetting values at some time interval.
         */
        ALL_TIME,

        /**
         * The leaderboard score values are reset at some given time interval.
         */
        EPOCHAL

    }

    public enum ScoreStrategyType {

        /**
         * When a new score value is provided, `MAX(old_score, new_score)` will be persisted to the store.
         */
        OVERWRITE_IF_GREATER,

        /**
         * When a new score value is provided, `old_score+new_score` will be persisted to the store.
         */
        ACCUMULATE

    }

}
