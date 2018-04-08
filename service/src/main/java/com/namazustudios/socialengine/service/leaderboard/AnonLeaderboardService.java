package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.service.LeaderboardService;

public class AnonLeaderboardService implements LeaderboardService {

    @Override
    public Pagination<Leaderboard> getLeaderboards(int offset, int count) {
        return null;
    }

    @Override
    public Pagination<Leaderboard> getLeaderboards(int offset, int count, String search) {
        return null;
    }

    @Override
    public Leaderboard getLeaderboard(String nameOrId) {
        return null;
    }

    @Override
    public Leaderboard createLeaderboard(Leaderboard application) {
        throw new NotFoundException();
    }

    @Override
    public Leaderboard updateLeaderboard(String nameOrId, Leaderboard application) {
        throw new NotFoundException();
    }

    @Override
    public void deleteLeaderboard(String nameOrId) {
        throw new NotFoundException();
    }

}
