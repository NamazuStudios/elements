package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.leaderboard.Rank;
import dev.getelements.elements.sdk.model.leaderboard.RankRow;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Provides access to {@link Rank} instances stored in the database, correlating and filtering data as necessary.
 */

@ElementServiceExport
public interface RankDao {

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset              the offset in the dataset
     * @param count               the number of results to return
     * @param leaderboardEpoch    the epoch timestamp
     * @return a {@link Pagination<Rank>} containing all ranks
     */
    Pagination<Rank> getRanksForGlobal(String leaderboardNameOrId, int offset, int count, long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the result set to be skipped forward to make the supplied {@link Profile} appear in the result
     * set.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId           the value of {@link Profile#getId()}
     * @param count               the number of results to return
     * @param leaderboardEpoch    the epoch timestamp
     * @return a {@link Pagination<Rank>} containing all ranks
     */
    Pagination<Rank> getRanksForGlobalRelative(String leaderboardNameOrId, String profileId,
                                               int offset, int count,
                                               long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the result set to be skipped forward to make the supplied {@link Profile} appear int the result
     * set.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId           the value of {@link Profile#getId()}
     * @param offset              the offset in the dataset, may be negative
     * @param count               the number of results to return
     * @param leaderboardEpoch    the epoch timestamp
     * @return a {@link Pagination<Rank>} containing all ranks
     */
    Pagination<Rank> getRanksForFriends(String leaderboardNameOrId,
                                        String profileId,
                                        int offset, int count,
                                        long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the result set to be skipped forward to make the supplied {@link Profile} appear int the result
     * set.
     * <p>
     * Additionally this will filter the results to only include friends of the supplied {@link Profile}.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId           the value of {@link Profile#getId()}
     * @param offset              the offset in the dataset, may be negative
     * @param count               the number of results to return   @return a {@link Pagination<Rank>} containing all ranks
     * @param leaderboardEpoch    the epoch timestamp
     */
    Pagination<Rank> getRanksForFriendsRelative(String leaderboardNameOrId,
                                                String profileId,
                                                int offset, int count,
                                                long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the result set to be skipped forward to make the supplied {@link Profile} appear int the result
     * set.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId           the value of {@link Profile#getId()}
     * @param offset              the offset in the dataset, may be negative
     * @param count               the number of results to return
     * @param leaderboardEpoch    the epoch timestamp
     * @return a {@link Pagination<Rank>} containing all ranks
     */
    Pagination<Rank> getRanksForMutualFollowers(
            String leaderboardNameOrId,
            String profileId,
            int offset, int count,
            long leaderboardEpoch);

    /**
     * Given the {@link Leaderboard} name or ID, this will return all {@link Rank} instances sorted in order.  This
     * allows the result set to be skipped forward to make the supplied {@link Profile} appear int the result
     * set.
     * <p>
     * Additionally this will filter the results to only include friends of the supplied {@link Profile}.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId           the value of {@link Profile#getId()}
     * @param offset              the offset in the dataset, may be negative
     * @param count               the number of results to return   @return a {@link Pagination<Rank>} containing all ranks
     * @param leaderboardEpoch    the epoch timestamp
     */
    Pagination<Rank> getRanksForMutualFollowersRelative(
            String leaderboardNameOrId,
            String profileId,
            int offset, int count,
            long leaderboardEpoch
    );

    /**
     * Gets the ranks for a leaderboard as a tabulation.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param leaderboardEpoch    the epoch timestamp
     * @return the tabular
     */
    Tabulation<RankRow> getRanksForGlobalTabular(String leaderboardNameOrId, long leaderboardEpoch);

}
