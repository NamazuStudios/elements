package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.dao.RankDao;
import com.namazustudios.socialengine.exception.UnauthorizedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.service.RankService;

import javax.inject.Inject;

public class AnonRankService implements RankService {

    private RankDao rankDao;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count, final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForGlobal(leaderboardNameOrId, offset, count, leaderboardEpoch)
            .transform(this::redactPrivateInfo);
    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId, final String profileId,
                                                      final int count, final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForGlobalRelative(leaderboardNameOrId, profileId, count, leaderboardEpoch)
            .transform(this::redactPrivateInfo);
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId,
                                               final int offset, final int count, final long leaderboardEpoch) {
        throw new UnauthorizedException();
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId,
                                                       final int offset, final int count, final long leaderboardEpoch) {
        throw new UnauthorizedException();
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
