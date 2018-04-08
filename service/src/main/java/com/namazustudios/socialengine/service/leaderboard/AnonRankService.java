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
    public Pagination<Rank> getRanksForFriends(final int offset, final int count) {
        throw new UnauthorizedException();
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final int offset, final int count, final String profileIdTrimmed) {
        throw new UnauthorizedException();
    }

    @Override
    public Pagination<Rank> getRanksForGlobal(final int offset, final int count) {
        return getRankDao().getRanksForGlobal(offset, count);
    }

    @Override
    public Pagination<Rank> getRanksForGlobal(final int offset, final int count, final String profileIdTrimmed) {
        return getRankDao().getRanksForGlobal(offset, count, profileIdTrimmed);
    }

    public RankDao getRankDao() {
        return rankDao;
    }

    @Inject
    public void setRankDao(RankDao rankDao) {
        this.rankDao = rankDao;
    }

}
