package com.namazustudios.socialengine.model.leaderboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel
public class Rank {

    @ApiModelProperty("The position of the associated score in the result set.")
    private int position;

    @NotNull
    @ApiModelProperty("The Score value for the particular rank")
    private Score score;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }
}
