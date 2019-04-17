package com.namazustudios.socialengine.model.gameon.game;

import com.namazustudios.socialengine.model.match.Match;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
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
    private Integer playerAttemptsPerMatch;

    @ApiModelProperty("The number of players per match.")
    private Integer playersPerMatch;

    @ApiModelProperty("The detailed listing of prize bundles.")
    private List<GameOnPrizeBundle> prizeBundles;

    @ApiModelProperty("The win type (e.g. highest).")
    private String winType;

    @ApiModelProperty("The score type (e.g. individual).")
    private String scoreType;

    @ApiModelProperty("Indicates whether the player has any UNCLAIMED, CLAIMED or FULFILLED prizes.")
    private String prizeBundleClaimStatus;

    @ApiModelProperty("The current state of the tournament.")
    private String tournamentState;

    @ApiModelProperty("The metadata of the tournament - text for any additional information (e.g. Rules, Disclaimers, etc.)")
    private String metadata;

    public String getPrizeBundleClaimStatus() {
        return prizeBundleClaimStatus;
    }

    public void setPrizeBundleClaimStatus(String prizeBundleClaimStatus) {
        this.prizeBundleClaimStatus = prizeBundleClaimStatus;
    }

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

    public Integer getPlayerAttemptsPerMatch() {
        return playerAttemptsPerMatch;
    }

    public void setPlayerAttemptsPerMatch(Integer playerAttemptsPerMatch) {
        this.playerAttemptsPerMatch = playerAttemptsPerMatch;
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


    public String getWinType() {
        return winType;
    }

    public void setWinType(String winType) {
        this.winType = winType;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public String getTournamentState() {
        return tournamentState;
    }

    public void setTournamentState(String tournamentState) {
        this.tournamentState = tournamentState;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
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
                Objects.equals(getPlayerAttemptsPerMatch(), that.getPlayerAttemptsPerMatch()) &&
                Objects.equals(getPlayersPerMatch(), that.getPlayersPerMatch()) &&
                Objects.equals(getPrizeBundles(), that.getPrizeBundles()) &&
                Objects.equals(getWinType(), that.getWinType()) &&
                Objects.equals(getScoreType(), that.getScoreType()) &&
                Objects.equals(getPrizeBundleClaimStatus(), that.getPrizeBundleClaimStatus()) &&
                Objects.equals(getTournamentState(), that.getTournamentState()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatchId(), getTournamentId(), getAttemptsRemaining(), getTitle(), getSubtitle(), getCanEnter(), getDateStart(), getDateEnd(), getImageUrl(), getMatchesPerPlayer(), getPlayerAttemptsPerMatch(), getPlayersPerMatch(), getPrizeBundles(), getWinType(), getScoreType(), getPrizeBundleClaimStatus(), getTournamentState(), getMetadata());
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
                ", playerAttemptsPerMatch=" + playerAttemptsPerMatch +
                ", playersPerMatch=" + playersPerMatch +
                ", prizeBundles=" + prizeBundles +
                ", winType=" + winType +
                ", scoreType=" + scoreType +
                ", prizeBundleClaimStatus=" + prizeBundleClaimStatus +
                ", tournamentState=" + tournamentState +
                ", metadata='" + metadata + '\'' +
                '}';
    }

}
