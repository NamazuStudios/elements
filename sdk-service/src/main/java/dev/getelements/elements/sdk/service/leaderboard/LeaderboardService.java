package dev.getelements.elements.sdk.service.leaderboard;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.LeaderboardNotFoundException;
import dev.getelements.elements.sdk.model.leaderboard.CreateLeaderboardRequest;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.leaderboard.UpdateLeaderboardRequest;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages instances of {@link Leaderboard}.
 * 
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface LeaderboardService {

    /**
     * Lists all leaderboards, specifying offset and count.
     *
     * @param offset the offset in the dataset
     * @param count  the number of results to return
     * @return the {@link Pagination<Leaderboard>}
     */
    Pagination<Leaderboard> getLeaderboards(int offset, int count);

    /**
     * Lists all leaderboards, specifying offset and count, as well as a search query.
     *
     * @param offset the offset in the dataset
     * @param count  the number of results to return
     * @return the {@link Pagination<Leaderboard>}
     */
    Pagination<Leaderboard> getLeaderboards(int offset, int count, String search);

    /**
     * Gets a single {@link Leaderboard} using the specified identifier or name.  Throws an
     * {@link LeaderboardNotFoundException} if not found.
     *
     * @param nameOrId the name or identifier {@link Leaderboard#getId()}, {@link Leaderboard#getName()}
     * @return the {@link Leaderboard} instance, never null
     */
    Leaderboard getLeaderboard(String nameOrId);

    /**
     * Creates a {@link Leaderboard} and returns the instance as it was written to the database.
     *
     * @param leaderboard the {@link Leaderboard}
     * @return the {@link Leaderboard}
     */
    Leaderboard createLeaderboard(CreateLeaderboardRequest leaderboard);

    /**
     * Updates an existing {@link Leaderboard} instance.  Throwing an instance of {@link LeaderboardNotFoundException}
     * if the {@link Leaderboard} does not exist.
     *
     * @param nameOrId the name or identifier {@link Leaderboard#getId()}, {@link Leaderboard#getName()}
     * @param request the {@link UpdateLeaderboardRequest} update request
     * @return the {@link Leaderboard}
     */
    Leaderboard updateLeaderboard(String nameOrId, UpdateLeaderboardRequest request);

    /**
     * Creates a {@link Leaderboard} and returns the instance as it was written to the database.
     *
     * @param leaderboard the {@link Leaderboard}
     * @return the {@link Leaderboard}
     */
    @Deprecated
    Leaderboard createLeaderboard(Leaderboard leaderboard);

    /**
     * Updates an existing {@link Leaderboard} instance.  Throwing an instance of {@link LeaderboardNotFoundException}
     * if the {@link Leaderboard} does not exist.
     *
     * @param nameOrId the name or identifier {@link Leaderboard#getId()}, {@link Leaderboard#getName()}
     * @param leaderboard         the {@link Leaderboard}
     * @return the {@link Leaderboard}
     */
    @Deprecated
    Leaderboard updateLeaderboard(String nameOrId, Leaderboard leaderboard);

    /**
     * Deletes the {@link Leaderboard} with the name or identifier.
     *
     * @param nameOrId the name or identifier {@link Leaderboard#getId()}, {@link Leaderboard#getName()}
     */
    void deleteLeaderboard(String nameOrId);

}
