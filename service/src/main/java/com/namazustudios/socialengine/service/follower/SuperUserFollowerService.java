package com.namazustudios.socialengine.service.follower;

import com.namazustudios.socialengine.dao.FollowerDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.follower.Follower;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.FollowerService;

import javax.inject.Inject;

public class SuperUserFollowerService implements FollowerService {

    private FollowerDao followerDao;

    @Override
    public Pagination<Profile> getFollowers(final String profileId, final int offset, final int count) {
        return getFollowerDao().getFollowersForProfile(profileId, offset, count).transform(this::redactPrivateInformation);
    }

    @Override
    public Profile getFollower(final String profileId, final String followedId) {
        return this.redactPrivateInformation(getFollowerDao().getFollowerForProfile(profileId, followedId));
    }

    @Override
    public void createFollower(final Follower follower) {
        getFollowerDao().createFollowerForProfile(follower);
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
