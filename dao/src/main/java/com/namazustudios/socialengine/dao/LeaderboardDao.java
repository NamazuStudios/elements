package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;

public interface LeaderboardDao {

    Pagination<Leaderboard> getLeaderboards(int offset, int count);

    Pagination<Leaderboard> getLeaderboards(int offset, int count, String search);

    Leaderboard getLeaderboard(String nameOrId);

    Leaderboard createLeaderboard(Leaderboard leaderboard);

    Leaderboard updateLeaderboard(Leaderboard leaderboard);

    void deleteLeaderboard(String nameOrId);

}
