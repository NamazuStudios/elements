package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.profile.Profile;

public interface RankDao {
    Pagination<Rank> getRanksForGlobal(int offset, int count);

    Pagination<Rank> getRanksForGlobal(int offset, int count, String profileIdTrimmed);

    Pagination<Rank> getRanksForFriends(Profile profile, int offset, int count);

    Pagination<Rank> getRanksForFriends(Profile profile, int offset, int count, String profileId);

}

