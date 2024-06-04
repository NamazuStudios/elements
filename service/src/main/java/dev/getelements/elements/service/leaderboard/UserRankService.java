package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.dao.RankDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.Tabulation;
import dev.getelements.elements.model.leaderboard.RankRow;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.leaderboard.Rank;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.RankService;
import dev.getelements.elements.service.largeobject.LargeObjectCdnUtils;

import javax.inject.Inject;
import java.util.function.Supplier;

public class UserRankService implements RankService {

    private User user;

    private RankDao rankDao;

    private Supplier<Profile> profileSupplier;

    private LargeObjectCdnUtils cdnUtils;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count,
                                              final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForGlobal(leaderboardNameOrId, offset, count, leaderboardEpoch)
            .transform(this::setupRank);
    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId,
                                                      final String profileId, int offset, final int count,
                                                      final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForGlobalRelative(leaderboardNameOrId, profileId, offset, count, leaderboardEpoch)
            .transform(this::setupRank);
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId,
                                               final int offset, final int count, final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForFriends(
                    leaderboardNameOrId,
                    getProfileSupplier().get().getId(),
                    offset, count,
                    leaderboardEpoch)
            .transform(this::setupRank);
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId,
                                                       final int offset, final int count, final long leaderboardEpoch) {
        return getRankDao()
            .getRanksForFriendsRelative(
                    leaderboardNameOrId,
                    getProfileSupplier().get().getId(),
                    offset, count,
                    leaderboardEpoch)
            .transform(this::setupRank);
    }

    @Override
    public Pagination<Rank> getRanksForMutualFollowers(final String leaderboardNameOrId,
                                                       final int offset, final int count,
                                                       final long leaderboardEpoch) {
        return getRankDao()
                .getRanksForMutualFollowers(
                        leaderboardNameOrId,
                        getProfileSupplier().get().getId(),
                        offset, count,
                        leaderboardEpoch)
                .transform(this::setupRank);
    }

    @Override
    public Pagination<Rank> getRanksForMutualFollowersRelative(final String leaderboardNameOrId,
                                                               final int offset, final int count,
                                                               final long leaderboardEpoch) {
        return getRankDao()
                .getRanksForMutualFollowersRelative(
                        leaderboardNameOrId,
                        getProfileSupplier().get().getId(),
                        offset, count,
                        leaderboardEpoch)
                .transform(this::setupRank);
    }

    private Rank setupRank(final Rank rank) {

        if (!getUser().equals(rank.getScore().getProfile().getUser())) {
            rank.getScore().getProfile().setUser(null);
        }

        getCdnUtils().setProfileCdnUrl(rank.getScore().getProfile());
        return rank;

    }

    @Override
    public Tabulation<RankRow> getRanksForGlobalTabular(final String leaderboardNameOrId, final long leaderboardEpoch) {
        throw new ForbiddenException();
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

    public LargeObjectCdnUtils getCdnUtils() {
        return cdnUtils;
    }

    @Inject
    public void setCdnUtils(LargeObjectCdnUtils cdnUtils) {
        this.cdnUtils = cdnUtils;
    }
}
