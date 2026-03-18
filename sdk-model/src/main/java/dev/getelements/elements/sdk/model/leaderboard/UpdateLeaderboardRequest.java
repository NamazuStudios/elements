package dev.getelements.elements.sdk.model.leaderboard;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/** Represents a request to update an existing leaderboard's properties. */
@Schema
public class UpdateLeaderboardRequest {

    /** Creates a new instance. */
    public UpdateLeaderboardRequest() {}

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
     * Returns the time strategy type for the leaderboard.
     *
     * @return the time strategy type
     */
    public Leaderboard.TimeStrategyType getTimeStrategyType() {
        return timeStrategyType;
    }

    /**
     * Sets the time strategy type for the leaderboard.
     *
     * @param timeStrategyType the time strategy type
     */
    public void setTimeStrategyType(Leaderboard.TimeStrategyType timeStrategyType) {
        this.timeStrategyType = timeStrategyType;
    }

    /**
     * Returns the score strategy type for the leaderboard.
     *
     * @return the score strategy type
     */
    public Leaderboard.ScoreStrategyType getScoreStrategyType() {
        return scoreStrategyType;
    }

    /**
     * Sets the score strategy type for the leaderboard.
     *
     * @param scoreStrategyType the score strategy type
     */
    public void setScoreStrategyType(Leaderboard.ScoreStrategyType scoreStrategyType) {
        this.scoreStrategyType = scoreStrategyType;
    }

    /**
     * Returns the user-presentable title for the leaderboard.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the user-presentable title for the leaderboard.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the units of measure for the score type of the leaderboard.
     *
     * @return the score units
     */
    public String getScoreUnits() {
        return scoreUnits;
    }

    /**
     * Sets the units of measure for the score type of the leaderboard.
     *
     * @param scoreUnits the score units
     */
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