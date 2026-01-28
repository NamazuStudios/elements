package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.sdk.dao.LeaderboardDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.leaderboard.CreateLeaderboardRequest;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;

import dev.getelements.elements.sdk.model.leaderboard.UpdateLeaderboardRequest;
import dev.getelements.elements.sdk.service.leaderboard.LeaderboardService;
import jakarta.inject.Inject;

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
    public Leaderboard createLeaderboard(CreateLeaderboardRequest request) {

        final var leaderboard = new Leaderboard();

        leaderboard.setName(request.getName());
        leaderboard.setTitle(request.getTitle());
        leaderboard.setScoreUnits(request.getScoreUnits());
        leaderboard.setScoreStrategyType(request.getScoreStrategyType());
        leaderboard.setEpochInterval(request.getEpochInterval());
        leaderboard.setFirstEpochTimestamp(request.getFirstEpochTimestamp());
        leaderboard.setTimeStrategyType(request.getTimeStrategyType());

        return getLeaderboardDao().createLeaderboard(leaderboard);
    }

    @Override
    public Leaderboard updateLeaderboard(String nameOrId, UpdateLeaderboardRequest request) {

        final var leaderboard = new Leaderboard();

        leaderboard.setName(request.getName());
        leaderboard.setTitle(request.getTitle());
        leaderboard.setScoreUnits(request.getScoreUnits());
        leaderboard.setScoreStrategyType(request.getScoreStrategyType());
        leaderboard.setTimeStrategyType(request.getTimeStrategyType());

        return getLeaderboardDao().updateLeaderboard(leaderboard);
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
