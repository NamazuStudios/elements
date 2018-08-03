package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.*;

/**
 * Used to manage the GameOn Matches.
 */
public interface GameOnMatchService {

    /**
     * Gets all matches, combining matches in developer tournaments as well as player-generated tournaments.
     *
     * @param deviceOSType the device OS type to use when referencing the session, may not be null
     * @param appBuildType the app build type to use when referencing the session, may not be null
     * @param filterBy specifies a filter for the matches, may be null
     * @param matchType specifices the types of matches to return, may be null
     * @param period specifies the period of the matches to filer
     * @param playerAttributes specifies the player attributes to use
     *
     * @return the {@link GameOnMatchesAggregate}, never null
     */
    GameOnMatchesAggregate getMatches(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        MatchFilter filterBy, MatchType matchType, TournamentPeriod period,
        String playerAttributes);

    /**
     * Gets a specific {@link GameOnMatchDetail} for a GameOn match with the suppplied ID and player attributes.
     * @param deviceOSType the device OS type to use when referencing the session, may not be null
     * @param appBuildType the app build type to use when referencing the session, may not be null
     * @param playerAttributes specifies the player attributes to use
     * @param matchId the specific match ID
     * @return the {@link GameOnMatchDetail}, never null
     */
    GameOnMatchDetail getMatch(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        String playerAttributes, String matchId);

    /**
     * Enters a match by suppling an instance of {@link GameOnEnterMatchRequest}.  Throwing the appropriate exception
     * if the request failed for any reason.
     *
     * @param matchId the GameOn assigned Match ID
     * @param gameOnEnterMatchRequest the {@link GameOnEnterMatchRequest} to use
     * @return the {@link GameOnEnterMatchResponse}, never null
     */
    GameOnEnterMatchResponse enterMatch(String matchId, GameOnEnterMatchRequest gameOnEnterMatchRequest);

}
