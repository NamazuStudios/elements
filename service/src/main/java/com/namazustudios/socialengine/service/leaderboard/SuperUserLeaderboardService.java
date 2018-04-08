package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.service.LeaderboardService;

import javax.inject.Inject;

public class SuperUserLeaderboardService implements LeaderboardService {

    private AnonLeaderboardService anonLeaderboardService;

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count) {
        return getAnonLeaderboardService().getLeaderboards(offset, count);
    }

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count, final String search) {
        return getAnonLeaderboardService().getLeaderboards(offset, count, search);
    }

    @Override
    public Leaderboard getLeaderboard(final String nameOrId) {
        return getAnonLeaderboardService().getLeaderboard(nameOrId);
    }

    @Override
    public Leaderboard createLeaderboard(final Leaderboard application) {
        return null;
    }

    @Override
    public Leaderboard updateLeaderboard(final String nameOrId, final Leaderboard application) {
        return null;
    }

    @Override
    public void deleteLeaderboard(final String nameOrId) {

    }

    public AnonLeaderboardService getAnonLeaderboardService() {
        return anonLeaderboardService;
    }

    @Inject
    public void setAnonLeaderboardService(AnonLeaderboardService anonLeaderboardService) {
        this.anonLeaderboardService = anonLeaderboardService;
    }

}
