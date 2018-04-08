package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.service.RankService;

import javax.inject.Inject;

public class UserRankService implements RankService {

    private AnonRankService anonRankService;

    @Override
    public Pagination<Rank> getRanksForFriends(final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final int offset, final int count, final String profileIdTrimmed) {
        return null;
    }

    @Override
    public Pagination<Rank> getRanksForGlobal(final int offset, final int count) {
        return getAnonRankService().getRanksForGlobal(offset, count);
    }

    @Override
    public Pagination<Rank> getRanksForGlobal(final int offset, final int count, final String profileIdTrimmed) {
        return getAnonRankService().getRanksForGlobal(offset, count, profileIdTrimmed);
    }

    public AnonRankService getAnonRankService() {
        return anonRankService;
    }

    @Inject
    public void setAnonRankService(AnonRankService anonRankService) {
        this.anonRankService = anonRankService;
    }

}
