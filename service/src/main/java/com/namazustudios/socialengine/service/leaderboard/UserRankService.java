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
    public Pagination<Rank> getRanksForFriends(final int offset, final int count) {
        return getRankDao().getRanksForFriends(profileSupplier.get(), offset, count);
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final int offset, final int count, final String profileId) {
        return getRankDao().getRanksForFriends(profileSupplier.get(), offset, count, profileId);
    }

    @Override
    public Pagination<Rank> getRanksForGlobal(final int offset, final int count) {
        return getRankDao().getRanksForGlobal(offset, count);
    }

    @Override
    public Pagination<Rank> getRanksForGlobal(final int offset, final int count, final String profileId) {
        return getRankDao().getRanksForGlobal(offset, count, profileId);
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
