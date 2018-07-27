package com.namazustudios.socialengine.service.gameon.client.model;

import com.namazustudios.socialengine.model.gameon.GameOnTournamentSummary;

import java.util.List;

public class GameOnTournamentListResponse {

    private List<GameOnTournamentSummary> tournaments;

    public List<GameOnTournamentSummary> getTournaments() {
        return tournaments;
    }

    public void setTournaments(List<GameOnTournamentSummary> tournaments) {
        this.tournaments = tournaments;
    }

}
