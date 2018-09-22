package com.namazustudios.socialengine.model.gameon;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Used in conjunction with the Match metadata field to specify additional GameOn tournament " +
                        "details")
public class TournamentEntryMetadata implements Serializable {

    @ApiModelProperty("The GameOn assigned match ID.")
    private String matchId;

    @ApiModelProperty("The GameOn assigned tournament ID.")
    private String tournamentId;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof TournamentEntryMetadata)) return false;
        TournamentEntryMetadata that = (TournamentEntryMetadata) object;
        return Objects.equals(getMatchId(), that.getMatchId()) &&
                Objects.equals(getTournamentId(), that.getTournamentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatchId(), getTournamentId());
    }

    @Override
    public String toString() {
        return "TournamentEntryMetadata{" +
                "matchId='" + matchId + '\'' +
                ", tournamentId='" + tournamentId + '\'' +
                '}';
    }

}
