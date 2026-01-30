package dev.getelements.elements.sdk.model.leaderboard;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

@Schema
public class CreateLeaderboardRequest {


    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    @Schema(description = "The name of the leaderboard.  This must be unique across all leaderboards.")
    private String name;

    @NotNull
    @Schema(description = "The time strategy for the leaderboard. Current options are ALL_TIME and EPOCHAL.")
    private Leaderboard.TimeStrategyType timeStrategyType;

    @NotNull
    @Schema(description = "The score strategy for the leaderboard. Current options are OVERWRITE_IF_GREATER and ACCUMULATE.")
    private Leaderboard.ScoreStrategyType scoreStrategyType;

    @NotNull
    @Schema(description = "The user-presentable name or title for for the leaderboard.")
    private String title;

    @NotNull
    @Schema(description = "The units-of measure for the score type of the leaderboard.")
    private String scoreUnits;

    @Min(0)
    @Schema(description = "The time at which the leaderboard epoch intervals should begin (in ms). If null, then " +
            "the leaderboard is all-time and not epochal. During creation, if this value is provided, then " +
            "epochInterval must also be provided.")
    private Long firstEpochTimestamp;

    @Min(0)
    @Schema(description = "The duration for a leaderboard epoch interval (in ms). If null, then " +
            "the leaderboard is all-time and not epochal. During creation, if this value is provided, then " +
            "firstEpochTimestamp must also be provided.")
    private Long epochInterval;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Leaderboard.TimeStrategyType getTimeStrategyType() {
        return timeStrategyType;
    }

    public void setTimeStrategyType(Leaderboard.TimeStrategyType timeStrategyType) {
        this.timeStrategyType = timeStrategyType;
    }

    public Leaderboard.ScoreStrategyType getScoreStrategyType() {
        return scoreStrategyType;
    }

    public void setScoreStrategyType(Leaderboard.ScoreStrategyType scoreStrategyType) {
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

    public Long getFirstEpochTimestamp() {
        return firstEpochTimestamp;
    }

    public void setFirstEpochTimestamp(Long firstEpochTimestamp) {
        this.firstEpochTimestamp = firstEpochTimestamp;
    }

    public Long getEpochInterval() {
        return epochInterval;
    }

    public void setEpochInterval(Long epochInterval) {
        this.epochInterval = epochInterval;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateLeaderboardRequest that)) return false;
        return Objects.equals(name, that.name) && timeStrategyType == that.timeStrategyType && scoreStrategyType == that.scoreStrategyType && Objects.equals(title, that.title) && Objects.equals(scoreUnits, that.scoreUnits) && Objects.equals(firstEpochTimestamp, that.firstEpochTimestamp) && Objects.equals(epochInterval, that.epochInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, timeStrategyType, scoreStrategyType, title, scoreUnits, firstEpochTimestamp, epochInterval);
    }

    @Override
    public String toString() {
        return "CreateLeaderboardRequest{" +
                "name='" + name + '\'' +
                ", timeStrategyType=" + timeStrategyType +
                ", scoreStrategyType=" + scoreStrategyType +
                ", title='" + title + '\'' +
                ", scoreUnits='" + scoreUnits + '\'' +
                ", firstEpochTimestamp=" + firstEpochTimestamp +
                ", epochInterval=" + epochInterval +
                '}';
    }
}