package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.dao.RankDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.RankService;

import javax.inject.Inject;
import java.util.function.Supplier;

public class UserRankService implements RankService {

    private User user;

    private RankDao rankDao;

    private Supplier<Profile> profileSupplier;

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
        return getRankDao()
            .getRanksForFriends(leaderboardNameOrId, getProfileSupplier().get(), offset, count, leaderboardEpoch)
            .transform(this::redactPrivateInfo);
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId,
                                                       final int offset, final int count, final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForFriendsRelative(leaderboardNameOrId, getProfileSupplier().get(), offset, count,
                    leaderboardEpoch)
            .transform(this::redactPrivateInfo);
    }

    private Rank redactPrivateInfo(final Rank rank) {

        if (!getUser().equals(rank.getScore().getProfile().getUser())) {
            rank.getScore().getProfile().setUser(null);
        }

        return rank;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
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
