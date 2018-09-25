package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.match.Match;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Represents a GameOn Match.  Maps direcly to the Amazon GameOn APIs.  Contains slightly less" +
                        "information than its detail counterpart.")
public class GameOnMatchSummary implements Serializable {

    @ApiModelProperty("The GameOn assigned match ID.")
    private String matchId;

    @ApiModelProperty("The GameOn assigned tournament ID.")
    private String tournamentId;

    @ApiModelProperty("The remaining attempts in this particular match.")
    private Integer attemptsRemaining;

    @ApiModelProperty("The title of the tournament")
    private String title;

    @ApiModelProperty("The subtitle of the tournament.")
    private String subtitle;

    @ApiModelProperty("True if the player can enter, false otherwise.")
    private Boolean canEnter;

    @ApiModelProperty("The date the tournament begins.")
    private Long dateStart;

    @ApiModelProperty("The date the tournament ends.")
    private Long dateEnd;

    @ApiModelProperty("The image URL for the tournament.")
    private String imageUrl;

    @ApiModelProperty("The nubmer of matches per player.")
    private Integer matchesPerPlayer;

    @ApiModelProperty("The number of attempts a player can make per match.")
    private Integer playerAttemtpsPerMatch;

    @ApiModelProperty("The number of players per match.")
    private Integer playersPerMatch;

    @ApiModelProperty("The detailed listing of prize bundles.")
    private List<GameOnPrizeBundle> prizeBundles;

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Integer getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public void setAttemptsRemaining(Integer attemptsRemaining) {
        this.attemptsRemaining = attemptsRemaining;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Boolean getCanEnter() {
        return canEnter;
    }

    public void setCanEnter(Boolean canEnter) {
        this.canEnter = canEnter;
    }

    public Long getDateStart() {
        return dateStart;
    }

    public void setDateStart(Long dateStart) {
        this.dateStart = dateStart;
    }

    public Long getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Long dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getMatchesPerPlayer() {
        return matchesPerPlayer;
    }

    public void setMatchesPerPlayer(Integer matchesPerPlayer) {
        this.matchesPerPlayer = matchesPerPlayer;
    }

    public Integer getPlayerAttemtpsPerMatch() {
        return playerAttemtpsPerMatch;
    }

    public void setPlayerAttemtpsPerMatch(Integer playerAttemtpsPerMatch) {
        this.playerAttemtpsPerMatch = playerAttemtpsPerMatch;
    }

    public Integer getPlayersPerMatch() {
        return playersPerMatch;
    }

    public void setPlayersPerMatch(Integer playersPerMatch) {
        this.playersPerMatch = playersPerMatch;
    }

    public List<GameOnPrizeBundle> getPrizeBundles() {
        return prizeBundles;
    }

    public void setPrizeBundles(List<GameOnPrizeBundle> prizeBundles) {
        this.prizeBundles = prizeBundles;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnMatchSummary)) return false;
        GameOnMatchSummary that = (GameOnMatchSummary) object;
        return Objects.equals(getMatchId(), that.getMatchId()) &&
                Objects.equals(getTournamentId(), that.getTournamentId()) &&
                Objects.equals(getAttemptsRemaining(), that.getAttemptsRemaining()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getSubtitle(), that.getSubtitle()) &&
                Objects.equals(getCanEnter(), that.getCanEnter()) &&
                Objects.equals(getDateStart(), that.getDateStart()) &&
                Objects.equals(getDateEnd(), that.getDateEnd()) &&
                Objects.equals(getImageUrl(), that.getImageUrl()) &&
                Objects.equals(getMatchesPerPlayer(), that.getMatchesPerPlayer()) &&
                Objects.equals(getPlayerAttemtpsPerMatch(), that.getPlayerAttemtpsPerMatch()) &&
                Objects.equals(getPlayersPerMatch(), that.getPlayersPerMatch()) &&
                Objects.equals(getPrizeBundles(), that.getPrizeBundles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatchId(), getTournamentId(), getAttemptsRemaining(), getTitle(), getSubtitle(), getCanEnter(), getDateStart(), getDateEnd(), getImageUrl(), getMatchesPerPlayer(), getPlayerAttemtpsPerMatch(), getPlayersPerMatch(), getPrizeBundles());
    }

    @Override
    public String toString() {
        return "GameOnMatchSummary{" +
                "matchId='" + matchId + '\'' +
                ", tournamentId='" + tournamentId + '\'' +
                ", attemptsRemaining=" + attemptsRemaining +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", canEnter=" + canEnter +
                ", dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", imageUrl='" + imageUrl + '\'' +
                ", matchesPerPlayer=" + matchesPerPlayer +
                ", playerAttemtpsPerMatch=" + playerAttemtpsPerMatch +
                ", playersPerMatch=" + playersPerMatch +
                ", prizeBundles=" + prizeBundles +
                '}';
    }

}
