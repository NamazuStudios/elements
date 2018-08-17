package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.GameOnPlayerTournamentEnterRequest;
import com.namazustudios.socialengine.model.gameon.GameOnPlayerTournamentEnterResponse;
import com.namazustudios.socialengine.model.gameon.game.*;

import java.util.List;

public interface GameOnPlayerTournamentService {

    /**
     * Gets all eligible tournaments based on the supplied inputs.  For a tournament to be eligible, the player must
     * meet all eligibility requirements and must not already be entered into the tournament.
     *
     * @param deviceOSType the device OS type, used to select the {@link GameOnSession} to use
     * @param appBuildType the app build type
     * @param filterBy the filtering parameters
     * @param period
     * @param playerAttributes the player attributes, specified as a string
     * @return a {@link List<GameOnTournamentSummary>}, never null
     */
    List<GameOnTournamentSummary> getEligibleTournaments(
            DeviceOSType deviceOSType, AppBuildType appBuildType,
            TournamentFilter filterBy, TournamentPeriod period,
            String playerAttributes);

    /**
     * Gets details for a specific tournament.  For a tournament to be eligible, the player must meet all eligibility
     * requirements and must not already be entered into the tournament.  If the supplied id would not otherwise
     * appear in the call to {@link #getEligibleTournaments(DeviceOSType, AppBuildType, TournamentFilter, TournamentPeriod, String)},
     * then this would throw the appropriate exception type to indicate that the tournament could not be found.
     *
     * @param deviceOSType the device OS type, used to select the {@link GameOnSession} to use
     * @param appBuildType the app build type
     * @param playerAttributes the player attributes, specified as a string
     * @return a {@link List<GameOnTournamentSummary>}, never null
     */
    GameOnTournamentDetail getEligibleTournamentDetail(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        String playerAttributes, String tournamentId);

    /**
     * Enters the current profile into a specific Game On tournament.
     *
     * @param tournamentId the tournament ID
     * @param gameOnPlayerTournamentEnterRequest the request to enter the tournamen
     * @return the {@link GameOnTournamentEnterResponse}, never null
     */
    GameOnPlayerTournamentEnterResponse enterTournament(
        String tournamentId,
        GameOnPlayerTournamentEnterRequest gameOnPlayerTournamentEnterRequest);

}
