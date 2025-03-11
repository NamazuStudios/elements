package dev.getelements.elements.sdk.service.leaderboard;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.leaderboard.Rank;
import dev.getelements.elements.sdk.model.leaderboard.RankRow;
import dev.getelements.elements.sdk.model.leaderboard.Score;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Queries and combines instances of {@link Leaderboard} and {@link Score} to calculate player rank.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface RankService {

    /**
     * Gets all {@link Rank} instances for the supplied {@link Leaderboard#getId()}
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForGlobal(String leaderboardNameOrId, int offset, int count, long leaderboardEpoch);

    /**
     * Gets all {@link Rank} instances for the supplied {@link Leaderboard} relative to the {@link Profile} with the
     * supplied id.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param profileId           the value of {@link Profile#getId()}
     * @param offset
     * @param count               the count
     * @param leaderboardEpoch    the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForGlobalRelative(String leaderboardNameOrId, String profileId, int offset, int count, long leaderboardEpoch);

    /**
     * Gets all {@link Rank} instances for the supplied {@link Leaderboard#getId()} relative to the {@link Profile} with
     * the supplied id returning only {@link Rank} instances within the supplied friend list for the given {@link Profile}
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForFriends(String leaderboardNameOrId, int offset, int count, long leaderboardEpoch);

    /**
     * Gets all {@link Rank} instances for the supplied {@link Leaderboard#getId()} relative to the {@link Profile} with
     * the supplied id returning only {@link Rank} instances within the supplied friend list for the given
     * {@link Profile}
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForFriendsRelative(String leaderboardNameOrId, int offset, int count,
                                                long leaderboardEpoch);

    /**
     * Gets all {@link Rank} instances for the supplied {@link Leaderboard#getId()} relative to the {@link Profile} with
     * the supplied id returning only {@link Rank} instances within the supplied friend list for the given
     * {@link Profile}
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForMutualFollowers(String leaderboardNameOrId,
                                                int offset, int count,
                                                long leaderboardEpoch);

    /**
     * Gets all {@link Rank} instances for the supplied {@link Leaderboard} relative to the {@link Profile} with the
     * supplied id returning only {@link Rank} instances within the supplied friend list for the given {@link Profile}
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForMutualFollowersRelative(String leaderboardNameOrId,
                                                        int offset, int count,
                                                        long leaderboardEpoch);


    /**
     * Gets the global ranks for tabular data.
     *
     * @return the tabular ranks
     */
    Tabulation<RankRow> getRanksForGlobalTabular(String leaderboardNameOrId, long leaderboardEpoch);

}
