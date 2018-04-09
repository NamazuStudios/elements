package com.namazustudios.socialengine.model.leaderboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel
public class Rank {

    @ApiModelProperty("The position of the associated score in the result set.")
    private long position;

    @NotNull
    @ApiModelProperty("The Score value for the particular rank")
    private Score score;

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public Score getScore() {
        return score;
    }

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
