package dev.getelements.elements.sdk.model.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;

/** Represents a player's rank on a leaderboard, consisting of a position and score. */
@Schema
public class Rank {

    /** Creates a new instance. */
    public Rank() {}

    @Schema(description = "The position of the associated score in the result set.")
    private long position;

    @NotNull
    @Schema(description = "The Score value for the particular rank")
    private Score score;

    /**
     * Returns the position of the associated score in the result set.
     *
     * @return the position
     */
    public long getPosition() {
        return position;
    }

    /**
     * Sets the position of the associated score in the result set.
     *
     * @param position the position
     */
    public void setPosition(long position) {
        this.position = position;
    }

    /**
     * Returns the score value for this rank.
     *
     * @return the score
     */
    public Score getScore() {
        return score;
    }

    /**
     * Sets the score value for this rank.
     *
     * @param score the score
     */
    public void setScore(Score score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rank)) return false;

        Rank rank = (Rank) o;

        if (getPosition() != rank.getPosition()) return false;
        return getScore() != null ? getScore().equals(rank.getScore()) : rank.getScore() == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (getPosition() ^ (getPosition() >>> 32));
        result = 31 * result + (getScore() != null ? getScore().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Rank{" +
                "position=" + position +
                ", score=" + score +
                '}';
    }

}
