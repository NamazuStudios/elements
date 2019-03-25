package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;

/**
 * Provides access to {@link Rank} instances stored in the database, correlating and filtering data as necessary.
 */
@Expose(modules = {
        "namazu.elements.dao.rank",
        "namazu.socialengine.dao.rank",
})
public interface RankDao {

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset in the dataset
     * @param count the number of results to return
     * @param leaderboardEpoch the epoch timestamp
     * @return a {@link Pagination<Rank>} containing all ranks
     */
    Pagination<Rank> getRanksForGlobal(String leaderboardNameOrId, int offset, int count, long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the the result set to be skipped forward to make the supplied {@link Profile} appear in= the result
     * set.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId the value of {@link Profile#getId()}
     * @param count the number of results to return
     * @param leaderboardEpoch the epoch timestamp
     * @return a {@link Pagination<Rank>} containing all ranks
     */
    Pagination<Rank> getRanksForGlobalRelative(String leaderboardNameOrId, String profileId, int count,
                                               long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the the result set to be skipped forward to make the supplied {@link Profile} appear int the result
     * set.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId the value of {@link Profile#getId()}
     * @param offset the offset in the dataset, may be negative
     * @param count the number of results to return
     * @param leaderboardEpoch the epoch timestamp
     * @return a {@link Pagination<Rank>} containing all ranks
     */
    Pagination<Rank> getRanksForFriends(String leaderboardNameOrId, Profile profileId, int offset, int count,
                                        long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the the result set to be skipped forward to make the supplied {@link Profile} appear int the result
     * set.
     *
     * Additionally this will filter the results to only include friends of the supplied {@link Profile}.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId the value of {@link Profile#getId()}
     * @param offset the offset in the dataset, may be negative
     * @param count the number of results to return   @return a {@link Pagination<Rank>} containing all ranks
     * @param leaderboardEpoch the epoch timestamp
     */
    Pagination<Rank> getRanksForFriendsRelative(String leaderboardNameOrId, Profile profileId, int offset, int count,
                                                long leaderboardEpoch);

}

