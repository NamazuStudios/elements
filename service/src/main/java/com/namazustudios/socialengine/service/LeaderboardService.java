package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Manages instances of {@link Leaderboard}.
 * 
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.scoped.leaderboard"),
    @ExposedModuleDefinition(value = "namazu.elements.service.unscoped.leaderboard", annotation = Unscoped.class)
})
public interface LeaderboardService {

    Pagination<Leaderboard> getLeaderboards(int offset, int count);

    Pagination<Leaderboard> getLeaderboards(int offset, int count, String search);

    Leaderboard getLeaderboard(String nameOrId);

    Leaderboard createLeaderboard(Leaderboard leaderboard);

    Leaderboard updateLeaderboard(String nameOrId, Leaderboard leaderboard);

    void deleteLeaderboard(String nameOrId);

}
