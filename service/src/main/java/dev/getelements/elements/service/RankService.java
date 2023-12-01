package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.model.leaderboard.Rank;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Queries and combines instances of {@link Leaderboard} and {@link Score} to calculate player rank.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.rank"
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.rank",
                deprecated = @DeprecationDefinition("Use eci.elements.service.rank instead.")
        )
})
public interface RankService {

    /**
     * Gets all {@link Rank} instances for the supplied {@Link Leaderboard#getId()} or {@Link Leaderboard#getRank()}.
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForGlobal(String leaderboardNameOrId, int offset, int count, long leaderboardEpoch);

    /**
     * Gets all {@link Rank} instances for the supplied {@Link Leaderboard#getId()} or {@Link Leaderboard#getRank()}
     * relative to the {@link Profile} with the supplied id.
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
     * Gets all {@link Rank} instances for the supplied {@Link Leaderboard#getId()} or {@Link Leaderboard#getRank()}
     * relative to the {@link Profile} with the supplied id returning only {@link Rank} instances within the supplied
     * friend list for the given {@link Profile}
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForFriends(String leaderboardNameOrId, int offset, int count, long leaderboardEpoch);

    /**
     * Gets all {@link Rank} instances for the supplied {@Link Leaderboard#getId()} or {@Link Leaderboard#getRank()}
     * relative to the {@link Profile} with the supplied id returning only {@link Rank} instances within the supplied
     * friend list for the given {@link Profile}
     *
     * @param leaderboardNameOrId the value of {@link Leaderboard#getId()} or {@link Leaderboard#getName()}
     * @param offset the offset
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForFriendsRelative(String leaderboardNameOrId, int offset, int count,
                                                long leaderboardEpoch);

}
