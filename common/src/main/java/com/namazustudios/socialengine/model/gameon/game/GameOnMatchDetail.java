package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.match.Match;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(description = "Represents a GameOn Match.  Maps direcly to the Amazon GameOn APIs.  Contains slightly more" +
                        "information than its summary counterpart.")
public class GameOnMatchDetail {

    @ApiModelProperty("The GameOn assigned match ID.")
    private String matchId;

    @ApiModelProperty("The details of the associated tournament.")
    private GameOnTournamentDetail tournamentDetails;

    @ApiModelProperty("Prizes awarded to the player.")
    private List<GameOnPrizeBundle> awardedPrizes;

    @ApiModelProperty("True if the player can enter, false otherwise.")
    private Boolean canEnter;

    @ApiModelProperty("The last score recorded for the match.")
    private Long lastScore;

    @ApiModelProperty("The date at which the last score was made.")
    private Long lastScoreDate;

    @ApiModelProperty("The player's overall score for the match.")
    private Long score;

    @ApiModelProperty("The date the score was submitted.")
    private Long scoreDate;

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public GameOnTournamentDetail getTournamentDetails() {
        return tournamentDetails;
    }

    public void setTournamentDetails(GameOnTournamentDetail tournamentDetails) {
        this.tournamentDetails = tournamentDetails;
    }

    public List<GameOnPrizeBundle> getAwardedPrizes() {
        return awardedPrizes;
    }

    public void setAwardedPrizes(List<GameOnPrizeBundle> awardedPrizes) {
        this.awardedPrizes = awardedPrizes;
    }

    public Boolean getCanEnter() {
        return canEnter;
    }

    public void setCanEnter(Boolean canEnter) {
        this.canEnter = canEnter;
    }

    public Long getLastScore() {
        return lastScore;
    }

    public void setLastScore(Long lastScore) {
        this.lastScore = lastScore;
    }

    public Long getLastScoreDate() {
        return lastScoreDate;
    }

    public void setLastScoreDate(Long lastScoreDate) {
        this.lastScoreDate = lastScoreDate;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Long getScoreDate() {
        return scoreDate;
    }

    public void setScoreDate(Long scoreDate) {
        this.scoreDate = scoreDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnMatchDetail)) return false;
        GameOnMatchDetail that = (GameOnMatchDetail) object;
        return Objects.equals(getMatchId(), that.getMatchId()) &&
                Objects.equals(getTournamentDetails(), that.getTournamentDetails()) &&
                Objects.equals(getAwardedPrizes(), that.getAwardedPrizes()) &&
                Objects.equals(getCanEnter(), that.getCanEnter()) &&
                Objects.equals(getLastScore(), that.getLastScore()) &&
                Objects.equals(getLastScoreDate(), that.getLastScoreDate()) &&
                Objects.equals(getScore(), that.getScore()) &&
                Objects.equals(getScoreDate(), that.getScoreDate());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getMatchId(), getTournamentDetails(), getAwardedPrizes(), getCanEnter(), getLastScore(), getLastScoreDate(), getScore(), getScoreDate());
    }

    @Override
    public String toString() {
        return "GameOnMatchDetail{" +
                "matchId='" + matchId + '\'' +
                ", tournamentDetails=" + tournamentDetails +
                ", awardedPrizes=" + awardedPrizes +
                ", canEnter=" + canEnter +
                ", lastScore=" + lastScore +
                ", lastScoreDate=" + lastScoreDate +
                ", score=" + score +
                ", scoreDate=" + scoreDate +
                '}';
    }

}
