package dev.getelements.elements.sdk.model.leaderboard;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

/** Represents a leaderboard for tracking and ranking player scores. */
@Schema
public class Leaderboard {

    /** Creates a new instance. */
    public Leaderboard() {}

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

    /**
     * Returns the unique ID of the leaderboard.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the leaderboard.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique name of the leaderboard.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the leaderboard.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the time strategy type.
     *
     * @return the time strategy type
     */
    public TimeStrategyType getTimeStrategyType() {
        return timeStrategyType;
    }

    /**
     * Sets the time strategy type.
     *
     * @param timeStrategyType the time strategy type
     */
    public void setTimeStrategyType(TimeStrategyType timeStrategyType) {
        this.timeStrategyType = timeStrategyType;
    }

    /**
     * Returns the score strategy type.
     *
     * @return the score strategy type
     */
    public ScoreStrategyType getScoreStrategyType() {
        return scoreStrategyType;
    }

    /**
     * Sets the score strategy type.
     *
     * @param scoreStrategyType the score strategy type
     */
    public void setScoreStrategyType(ScoreStrategyType scoreStrategyType) {
        this.scoreStrategyType = scoreStrategyType;
    }

    /**
     * Returns the display title of the leaderboard.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the display title of the leaderboard.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the score units of measure.
     *
     * @return the score units
     */
    public String getScoreUnits() {
        return scoreUnits;
    }

    /**
     * Sets the score units of measure.
     *
     * @param scoreUnits the score units
     */
    public void setScoreUnits(String scoreUnits) {
        this.scoreUnits = scoreUnits;
    }

    /**
     * Returns the first epoch timestamp in milliseconds.
     *
     * @return the first epoch timestamp
     */
    public Long getFirstEpochTimestamp() { return firstEpochTimestamp; }

    /**
     * Sets the first epoch timestamp in milliseconds.
     *
     * @param firstEpochTimestamp the first epoch timestamp
     */
    public void setFirstEpochTimestamp(Long firstEpochTimestamp) { this.firstEpochTimestamp = firstEpochTimestamp; }

    /**
     * Returns the epoch interval duration in milliseconds.
     *
     * @return the epoch interval
     */
    public Long getEpochInterval() { return epochInterval; }

    /**
     * Sets the epoch interval duration in milliseconds.
     *
     * @param epochInterval the epoch interval
     */
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

    /** Defines the time strategy for the leaderboard. */
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

    /** Defines the score strategy for the leaderboard. */
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
