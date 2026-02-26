package dev.getelements.elements.sdk.model.leaderboard;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

@Schema
public class UpdateLeaderboardRequest {

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UpdateLeaderboardRequest that)) return false;
        return Objects.equals(name, that.name) && timeStrategyType == that.timeStrategyType && scoreStrategyType == that.scoreStrategyType && Objects.equals(title, that.title) && Objects.equals(scoreUnits, that.scoreUnits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, timeStrategyType, scoreStrategyType, title, scoreUnits);
    }

    @Override
    public String toString() {
        return "UpdateLeaderboardRequest{" +
                "name='" + name + '\'' +
                ", timeStrategyType=" + timeStrategyType +
                ", scoreStrategyType=" + scoreStrategyType +
                ", title='" + title + '\'' +
                ", scoreUnits='" + scoreUnits + '\'' +
                '}';
    }
}