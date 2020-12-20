package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Queries and combines instances of {@link Leaderboard} and {@link Score} to calculate player rank.
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.scoped.rank"),
    @ExposedModuleDefinition(value = "namazu.elements.service.unscoped.rank", annotation = Unscoped.class)
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
     * @param profileId the value of {@link Profile#getId()}
     * @param count the count
     * @param leaderboardEpoch the epoch timestamp
     * @return the {@link Pagination<Rank>}
     */
    Pagination<Rank> getRanksForGlobalRelative(String leaderboardNameOrId, String profileId, int count,
                                               long leaderboardEpoch);

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
