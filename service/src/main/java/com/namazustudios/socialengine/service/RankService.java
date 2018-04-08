package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;

public interface RankService {

    Pagination<Rank> getRanksForFriends(int offset, int count);

    Pagination<Rank> getRanksForFriends(int offset, int count, String profileIdTrimmed);

    Pagination<Rank> getRanksForGlobal(int offset, int count);

    Pagination<Rank> getRanksForGlobal(int offset, int count, String profileIdTrimmed);

}
