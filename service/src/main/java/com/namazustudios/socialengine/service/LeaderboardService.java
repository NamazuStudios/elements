package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;

/**
 * Manages instances of {@link Leaderboard}.
 * 
 */
public interface LeaderboardService {

    Pagination<Leaderboard> getLeaderboards(int offset, int count);

    Pagination<Leaderboard> getLeaderboards(int offset, int count, String search);

    Leaderboard getLeaderboard(String nameOrId);

    Leaderboard createLeaderboard(Leaderboard application);

    Leaderboard updateLeaderboard(String nameOrId, Leaderboard application);

    void deleteLeaderboard(String nameOrId);

}
