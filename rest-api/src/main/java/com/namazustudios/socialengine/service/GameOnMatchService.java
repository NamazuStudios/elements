package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.*;

public interface GameOnMatchService {

    GameOnMatchesAggregate getMatches(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        MatchFilter filterBy, MatchType matchType, TournamentPeriod period,
        String playerAttributes);

    GameOnTournamentDetail getMatch(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        String playerAttributes, String matchId);

}
