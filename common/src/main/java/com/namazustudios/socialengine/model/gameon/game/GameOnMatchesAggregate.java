package com.namazustudios.socialengine.model.gameon.game;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Aggregates standard matches and player matches.")
public class GameOnMatchesAggregate implements Serializable {

    private List<GameOnMatchSummary> matches;

    private List<GameOnPlayerMatchSummary> playerMatches;

    public List<GameOnMatchSummary> getMatches() {
        return matches;
    }

    public void setMatches(List<GameOnMatchSummary> matches) {
        this.matches = matches;
    }

    public List<GameOnPlayerMatchSummary> getPlayerMatches() {
        return playerMatches;
    }

    public void setPlayerMatches(List<GameOnPlayerMatchSummary> playerMatches) {
        this.playerMatches = playerMatches;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GameOnMatchesAggregate)) return false;
        GameOnMatchesAggregate that = (GameOnMatchesAggregate) object;
        return Objects.equals(getMatches(), that.getMatches()) &&
                Objects.equals(getPlayerMatches(), that.getPlayerMatches());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatches(), getPlayerMatches());
    }

}
