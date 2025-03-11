package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.sdk.dao.LeaderboardDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;

import dev.getelements.elements.sdk.service.leaderboard.LeaderboardService;
import jakarta.inject.Inject;

public class AnonLeaderboardService implements LeaderboardService {

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

    public LeaderboardDao getLeaderboardDao() {
        return leaderboardDao;
    }

    @Inject
    public void setLeaderboardDao(LeaderboardDao leaderboardDao) {
        this.leaderboardDao = leaderboardDao;
    }

}
