package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.dao.RankDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.UnauthorizedException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.leaderboard.Rank;
import dev.getelements.elements.service.RankService;

import javax.inject.Inject;

public class AnonRankService implements RankService {

    private RankDao rankDao;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count,
                                              final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForGlobal(
                    leaderboardNameOrId,
                    offset, count,
                    leaderboardEpoch)
            .transform(this::redactPrivateInfo);
    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId,
                                                      final String profileId,
                                                      int offset, final int count,
                                                      final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForGlobalRelative(
                    leaderboardNameOrId,
                    profileId,
                    offset, count,
                    leaderboardEpoch)
            .transform(this::redactPrivateInfo);
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId,
                                               final int offset, final int count,
                                               final long leaderboardEpoch) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId,
                                                       final int offset, final int count,
                                                       final long leaderboardEpoch) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Rank> getRanksForMutualFollowers(final String leaderboardNameOrId,
                                                       final int offset, final int count,
                                                       final long leaderboardEpoch) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Rank> getRanksForMutualFollowersRelative(final String leaderboardNameOrId,
                                                               final int offset, final int count,
                                                               final long leaderboardEpoch) {
        throw new ForbiddenException();
    }

    private Rank redactPrivateInfo(final Rank rank) {
        rank.getScore().getProfile().setUser(null);
        return rank;
    }

    public RankDao getRankDao() {
        return rankDao;
    }

    @Inject
    public void setRankDao(RankDao rankDao) {
        this.rankDao = rankDao;
    }

}
