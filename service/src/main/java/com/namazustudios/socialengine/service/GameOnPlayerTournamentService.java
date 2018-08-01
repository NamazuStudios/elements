package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.*;

import java.util.List;

public interface GameOnPlayerTournamentService {

    List<GameOnTournamentSummary> getEligibleTournaments(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        TournamentFilter filterBy, TournamentPeriod period,
        String playerAttributes);

    GameOnTournamentDetail getEligibleTournamentDetail(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        String playerAttributes, String tournamentId);

}
