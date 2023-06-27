package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.dao.LeaderboardDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.service.LeaderboardService;

import javax.inject.Inject;

public class SuperUserLeaderboardService implements LeaderboardService {

    private LeaderboardDao leaderboardDao;

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count) {
        return getLeaderboardDao().getLeaderboards(offset, count);
    }

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count, final String search) {
        return getLeaderboardDao().getLeaderboards(offset, count, search);
    }

    @Override
    public Leaderboard getLeaderboard(final String nameOrId) {
        return getLeaderboardDao().getLeaderboard(nameOrId);
    }

    @Override
    public Leaderboard createLeaderboard(final Leaderboard leaderboard) {
        return getLeaderboardDao().createLeaderboard(leaderboard);
    }

    @Override
    public Leaderboard updateLeaderboard(final String leaderboardNameOrId, final Leaderboard leaderboard) {
        return getLeaderboardDao().updateLeaderboard(leaderboardNameOrId, leaderboard);
    }

    @Override
    public void deleteLeaderboard(final String nameOrId) {
        getLeaderboardDao().deleteLeaderboard(nameOrId);
    }

    public LeaderboardDao getLeaderboardDao() {
        return leaderboardDao;
    }

    @Inject
    public void setLeaderboardDao(LeaderboardDao leaderboardDao) {
        this.leaderboardDao = leaderboardDao;
    }

}
