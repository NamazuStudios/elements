package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.dao.RankDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.RankService;

import javax.inject.Inject;
import java.util.function.Supplier;

public class UserRankService implements RankService {

    private RankDao rankDao;

    private Supplier<Profile> profileSupplier;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count) {
        return getRankDao().getRanksForGlobal(leaderboardNameOrId, offset, count);
    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId, final String profileId,
                                                      final int offset, final int count) {
        return getRankDao().getRanksForGlobalRelative(leaderboardNameOrId, profileId, offset, count);
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId,
                                               final int offset, final int count) {
        return getRankDao().getRanksForFriends(leaderboardNameOrId, getProfileSupplier().get(), offset, count);
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId,
                                                       final int offset, final int count) {
        return getRankDao().getRanksForFriendsRelative(leaderboardNameOrId, getProfileSupplier().get(), offset, count);
    }

    public RankDao getRankDao() {
        return rankDao;
    }

    @Inject
    public void setRankDao(RankDao rankDao) {
        this.rankDao = rankDao;
    }

    public Supplier<Profile> getProfileSupplier() {
        return profileSupplier;
    }

    @Inject
    public void setProfileSupplier(Supplier<Profile> profileSupplier) {
        this.profileSupplier = profileSupplier;
    }

}
