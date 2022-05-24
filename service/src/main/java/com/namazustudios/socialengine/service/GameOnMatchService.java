package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.game.GameOnEnterMatchRequest;
import com.namazustudios.socialengine.model.gameon.game.GameOnEnterMatchResponse;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

/**
 * Used to manage the GameOn Matches.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.gameon.match")
})
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
     * Enters a match by supplying an instance of {@link GameOnEnterMatchRequest}.  Throwing the appropriate exception
     * if the request failed for any reason.
     *
     * @param matchId the GameOn assigned Match ID
     * @param gameOnEnterMatchRequest the {@link GameOnEnterMatchRequest} to use
     * @return the {@link GameOnEnterMatchResponse}, never null
     */
    GameOnEnterMatchResponse enterMatch(String matchId, GameOnEnterMatchRequest gameOnEnterMatchRequest);

    /**
     * Returns the {@link GameOnGetMatchLeaderboardResponse} for the supplied match.
     *
     * @param deviceOSType the device OS type to use when referencing the session, may not be null
     * @param appBuildType the app build type to use when referencing the session, may not be null
     * @param matchId the game-on match id
     * @param currentPlayerNeighbors the number of current neighbors to request
     * @param limit the limit to fetch
     * @param cursor the cursor from which to fetch the next page
     * @return the {@link GameOnGetMatchLeaderboardResponse}
     */
    GameOnGetMatchLeaderboardResponse getLeaderboard(DeviceOSType deviceOSType, AppBuildType appBuildType,
                                                     String matchId, Integer currentPlayerNeighbors, Integer limit,
                                                     String cursor);

}
