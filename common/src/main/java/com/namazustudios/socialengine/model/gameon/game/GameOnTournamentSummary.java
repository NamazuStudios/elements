package com.namazustudios.socialengine.model.gameon.game;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApiModel(description = "Maps to the response from a GameOn Tournament object from the AWS GameOn API.")
public class GameOnTournamentSummary implements Serializable {

    @ApiModelProperty("The GameOn assigned tournament ID.")
    private String tournamentId;

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

    @ApiModelProperty("The number of matches per player.")
    private Integer matchesPerPlayer;

    @ApiModelProperty("The number of attempts a player can make per match.")
    private Integer playerAttemptsPerMatch;

    @ApiModelProperty("The number of players per match.")
    private Integer playersPerMatch;

    @ApiModelProperty("The detailed listing of prize bundles.")
    private List<GameOnPrizeBundle> prizeBundles;

    @ApiModelProperty("The win type (e.g. highest).")
    private String winType;

    @ApiModelProperty("The description of the tournament.")
    private String description;

    @ApiModelProperty("Whether or not an access key is required to enter the tournament.")
    private Boolean hasAccessKey;

    @ApiModelProperty("The score type (e.g. individual).")
    private String scoreType;

    @ApiModelProperty("The current state of the tournament (e.g. open, closed).")
    private TournamentState tournamentState;

    @ApiModelProperty("The types of participants that will be in the tournament (e.g. individual, team).")
    private String participantType;

    @ApiModelProperty("The metadata of the tournament - text for any additional information (e.g. Rules, Disclaimers, etc.)")
    private String metadata;

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
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

    public String getWinType() { return winType; }

    public void setWinType(String winType) { this.winType = winType; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Boolean getHasAccessKey() { return hasAccessKey; }

    public void setHasAccessKey(Boolean hasAccessKey) { this.hasAccessKey = hasAccessKey; }

    public String getScoreType() { return scoreType; }

    public void setScoreType(String scoreType) { this.scoreType = scoreType; }

    public TournamentState getTournamentState() { return tournamentState; }

    public void setTournamentState(TournamentState tournamentState) { this.tournamentState = tournamentState; }

    public String getParticipantType() { return participantType; }

    public void setParticipantType(String participantType) { this.participantType = participantType; }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnTournamentSummary)) return false;
        GameOnTournamentSummary that = (GameOnTournamentSummary) object;
        return Objects.equals(getTournamentId(), that.getTournamentId()) &&
                Objects.equals(getCanEnter(), that.getCanEnter()) &&
                Objects.equals(getDateStart(), that.getDateStart()) &&
                Objects.equals(getDateEnd(), that.getDateEnd()) &&
                Objects.equals(getImageUrl(), that.getImageUrl()) &&
                Objects.equals(getMatchesPerPlayer(), that.getMatchesPerPlayer()) &&
                Objects.equals(getPlayerAttemptsPerMatch(), that.getPlayerAttemptsPerMatch()) &&
                Objects.equals(getPlayersPerMatch(), that.getPlayersPerMatch()) &&
                Objects.equals(getPrizeBundles(), that.getPrizeBundles()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getSubtitle(), that.getSubtitle()) &&
                Objects.equals(getWinType(), that.getWinType()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getHasAccessKey(), that.getHasAccessKey()) &&
                Objects.equals(getScoreType(), that.getScoreType()) &&
                Objects.equals(getTournamentState(), that.getTournamentState()) &&
                Objects.equals(getParticipantType(), that.getParticipantType()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTournamentId(), getCanEnter(), getDateStart(), getDateEnd(), getImageUrl(),
                getMatchesPerPlayer(), getPlayerAttemptsPerMatch(), getPlayersPerMatch(), getPrizeBundles(), getTitle(),
                getSubtitle(), getWinType(), getDescription(), getHasAccessKey(), getScoreType(),
                getTournamentState(), getParticipantType(), getMetadata());
    }

    @Override
    public String toString() {
        return "GameOnTournamentSummary{" +
                "tournamentId='" + tournamentId + '\'' +
                ", canEnter=" + canEnter +
                ", dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", imageUrl='" + imageUrl + '\'' +
                ", matchesPerPlayer=" + matchesPerPlayer +
                ", playerAttemptsPerMatch=" + playerAttemptsPerMatch +
                ", playersPerMatch=" + playersPerMatch +
                ", prizeBundles=" + prizeBundles +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", winType='" + winType + '\'' +
                ", description='" + description + '\'' +
                ", hasAccessKey='" + hasAccessKey + '\'' +
                ", scoreType='" + scoreType + '\'' +
                ", tournamentState='" + tournamentState + '\'' +
                ", participantType='" + participantType + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }

    @ApiModel(description = "Current state of the tournament (e.g. open, closed). See: " +
            "https://developer.amazon.com/docs/gameon/game-api-ref.html#getmatchdetailsresponse_tournamentdetails")
    public enum TournamentState {

        /**
         * The tournament is upcoming or on-going.
         */
        OPEN,

        /**
         * The tournament is in the process of closing.
         */
        WAITING_TO_BE_CLOSED,

        /**
         * The tournament has closed and is awaiting manual approval in Developer Console.
         */
        CLOSED,

        /**
         * The tournament is in the process of completing.
         */
        WAITING_TO_BE_COMPLETED,

        /**
         * The tournament has finalized and prizes have been awarded.
         */
        COMPLETED
    }

}
