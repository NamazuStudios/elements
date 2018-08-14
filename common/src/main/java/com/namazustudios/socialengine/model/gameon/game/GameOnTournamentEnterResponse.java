package com.namazustudios.socialengine.model.gameon.game;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(description = "The response returned when entering a tournament.")
public class GameOnTournamentEnterResponse {

    @ApiModelProperty("The GameOn Match ID that was created in response to the request.")
    private String matchId;

    @ApiModelProperty("The GameOn Tournament ID that was entered in response to the request.")
    private String tournamentId;

    @ApiModelProperty("The player's remaining attempts in the tournament.")
    private Integer attemptsRemaining;

    @ApiModelProperty("The tournament metadata that was used to create the tournament.  May be null.")
    private String metadata;

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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnTournamentEnterResponse)) return false;
        GameOnTournamentEnterResponse that = (GameOnTournamentEnterResponse) object;
        return Objects.equals(getMatchId(), that.getMatchId()) &&
                Objects.equals(getTournamentId(), that.getTournamentId()) &&
                Objects.equals(getAttemptsRemaining(), that.getAttemptsRemaining()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatchId(), getTournamentId(), getAttemptsRemaining(), getMetadata());
    }

    @Override
    public String toString() {
        return "GameOnTournamentEnterResponse{" +
                "matchId='" + matchId + '\'' +
                ", tournamentId='" + tournamentId + '\'' +
                ", attemptsRemaining=" + attemptsRemaining +
                ", metadata='" + metadata + '\'' +
                '}';
    }

}
