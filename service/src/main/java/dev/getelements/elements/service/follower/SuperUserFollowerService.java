package dev.getelements.elements.service.follower;

import dev.getelements.elements.dao.FollowerDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.FollowerService;

import javax.inject.Inject;

public class SuperUserFollowerService implements FollowerService {

    private FollowerDao followerDao;

    @Override
    public Pagination<Profile> getFollowers(final String profileId,
                                            final int offset,
                                            final int count) {
        return getFollowerDao().getFollowersForProfile(profileId, offset, count);
    }

    @Override
    public Pagination<Profile> getFollowees(final String profileId,
                                            final int offset,
                                            final int count) {
        return getFollowerDao().getFolloweesForProfile(profileId, offset, count);
    }

    @Override
    public Profile getFollower(final String profileId, final String followedId) {
        return this.redactPrivateInformation(getFollowerDao().getFollowerForProfile(profileId, followedId));
    }

    @Override
    public void createFollower(final String profileId, final CreateFollowerRequest createFollowerRequest) {
        getFollowerDao().createFollowerForProfile(profileId, createFollowerRequest);
    }

    @Override
    public void deleteFollower(final String profileId, final String profileToUnfollowId) {
        getFollowerDao().deleteFollowerForProfile(profileId, profileToUnfollowId);
    }

    public FollowerDao getFollowerDao() {
        return followerDao;
    }

    @Inject
    public void setFollowerDao(FollowerDao followerDao) {
        this.followerDao = followerDao;
    }

}
