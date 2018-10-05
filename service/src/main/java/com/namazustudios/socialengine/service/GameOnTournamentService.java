package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.gameon.game.*;

import java.util.List;

/**
 * Handles interaction with the {@link GameOnTournamentDetail} and the {@link GameOnTournamentSummary}.  This interacts
 * with the GameOn APIs as well as performs any filtering necessary to determine which instances to show to the client.
 */
public interface GameOnTournamentService {


    /**
     * Gets all eligible tournaments based on the supplied inputs.
     *
     * @param deviceOSType the device OS type, used to select the {@link GameOnSession} to use
     * @param appBuildType the application build type, used to select the {@link GameOnSession} to use
     * @param filterBy the filtering parameters
     * @param period filtering by time period
     * @param playerAttributes the player attributes, specified as a string
     * @return a {@link List<GameOnTournamentSummary>}, never null
     */
    List<GameOnTournamentSummary> getTournaments(
        DeviceOSType deviceOSType, AppBuildType appBuildType,
        TournamentFilter filterBy, TournamentPeriod period, String playerAttributes);

    /**
     * Gets all eligible tournaments based on the supplied inputs.  For a tournament to be eligible, the player must
     * meet all eligibility requirements and must not already be entered into the tournament.
     *
     * @param deviceOSType the device OS type, used to select the {@link GameOnSession} to use
     * @param appBuildType the application build type, used to select the {@link GameOnSession} to use
     * @param filterBy the filtering parameters
     * @param period filtering by time period
     * @param playerAttributes the player attributes, specified as a string
     * @return a {@link List<GameOnTournamentSummary>}, never null
     */
    List<GameOnTournamentSummary> getEligibleTournaments(
            DeviceOSType deviceOSType,
            AppBuildType appBuildType, TournamentFilter filterBy,
            TournamentPeriod period, String playerAttributes);

    /**
     * Gets details for a specific tournament.
     *
     * @param deviceOSType the device OS type, used to select the {@link GameOnSession} to use
     * @param appBuildType the application build type, used to select the {@link GameOnSession} to use
     * @param playerAttributes the player attributes, specified as a string
     * @param tournamentId the unique id of the tournament
     * @return the {@link GameOnTournamentDetail}, never null
     */
    GameOnTournamentDetail getTournamentDetail(
            DeviceOSType deviceOSType, AppBuildType appBuildType,
            String playerAttributes, String tournamentId);

    /**
     * Gets details for a specific tournament.  For a tournament to be eligible, the player must meet all eligibility
     * requirements and must not already be entered into the tournament.  If the supplied id would not otherwise
     * appear in the call to {@link #getEligibleTournaments(DeviceOSType, AppBuildType, TournamentFilter, TournamentPeriod, String)}, then this would
     * throw the appropriate exception type to indicate that the tournament could not be found.
     *
     * @param deviceOSType the device OS type, used to select the {@link GameOnSession} to use
     * @param appBuildType the application build type, used to select the {@link GameOnSession} to use
     * @param playerAttributes the player attributes, specified as a string
     * @param tournamentId the unique id of the tournament
     * @return the {@link GameOnTournamentDetail}, never null
     */
    GameOnTournamentDetail getEligibleTournamentDetail(
            DeviceOSType deviceOSType, AppBuildType appBuildType,
            String playerAttributes, String tournamentId);

    /**
     * Enters the current profile into a specific Game On tournament.
     *
     * @param tournamentId the tournament ID
     * @param gameOnTournamentEnterRequest the request to enter the tournamen
     * @return the {@link GameOnTournamentEnterResponse}, never null
     */
    GameOnTournamentEnterResponse enterTournament(
            String tournamentId,
            GameOnTournamentEnterRequest gameOnTournamentEnterRequest);

}
