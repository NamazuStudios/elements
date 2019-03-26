package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.LeaderboardNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.rt.annotation.Expose;

/**
 * Provides CRUD operations for Leaderboard instances in the database.
 */
@Expose(modules = {
        "namazu.elements.dao.leaderboard",
        "namazu.socialengine.dao.leaderboard",
})
public interface LeaderboardDao {

    /**
     * Lists all leaderboards, specifying offset and count.
     *
     * @param offset the offset in the dataset
     * @param count the number or results to return
     * @return the {@link Pagination<Leaderboard>}
     */
    Pagination<Leaderboard> getLeaderboards(int offset, int count);

    /**
     * Lists all leaderboards, specifying offset and count, as well as a search query.
     *
     * @param offset the offset in the dataset
     * @param count the number or results to return
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
    Leaderboard createLeaderboard(Leaderboard leaderboard);

    /**
     * Updates an existing {@link Leaderboard} instance.  Throwing an instance of {@link LeaderboardNotFoundException}
     * if the {@link Leaderboard} does not exist.
     *
     *
     * @param leaderboardNameOrId the name or identifier {@link Leaderboard#getId()}, {@link Leaderboard#getName()}
     * @param leaderboard the {@link Leaderboard}
     * @return the {@link Leaderboard}
     */
    Leaderboard updateLeaderboard(String leaderboardNameOrId, Leaderboard leaderboard);

    /**
     * Deletes the {@link Leaderboard} with the name or identifier.
     *
     * @param nameOrId the name or identifier {@link Leaderboard#getId()}, {@link Leaderboard#getName()}
     */
    void deleteLeaderboard(String nameOrId);

}
