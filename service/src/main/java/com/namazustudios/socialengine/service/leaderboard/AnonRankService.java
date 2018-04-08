package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.exception.UnauthorizedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.service.RankService;

public class AnonRankService implements RankService {

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
        return null;
    }

    @Override
    public Pagination<Rank> getRanksForGlobal(final int offset, final int count, final String profileIdTrimmed) {
        return null;
    }

}
