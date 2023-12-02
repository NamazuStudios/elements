package dev.getelements.elements.service.follower;

import dev.getelements.elements.dao.FollowerDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.FollowerService;
import dev.getelements.elements.service.largeobject.LargeObjectCdnUtils;

import javax.inject.Inject;

public class SuperUserFollowerService implements FollowerService {

    private FollowerDao followerDao;

    private LargeObjectCdnUtils cdnUtils;

    @Override
    public Pagination<Profile> getFollowers(final String profileId,
                                            final int offset,
                                            final int count) {
        Pagination<Profile> followers = getFollowerDao().getFollowersForProfile(profileId, offset, count);
        followers.getObjects().forEach(profile -> getCdnUtils().setProfileCdnUrl(profile));
        return followers;
    }

    @Override
    public Pagination<Profile> getFollowees(final String profileId,
                                            final int offset,
                                            final int count) {
        Pagination<Profile> followees = getFollowerDao().getFolloweesForProfile(profileId, offset, count);
        followees.getObjects().forEach(profile -> getCdnUtils().setProfileCdnUrl(profile));
        return followees;
    }

    @Override
    public Profile getFollower(final String profileId, final String followedId) {
        Profile profile = this.redactPrivateInformation(getFollowerDao().getFollowerForProfile(profileId, followedId));
        return getCdnUtils().setProfileCdnUrl(profile);
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

    public LargeObjectCdnUtils getCdnUtils() {
        return cdnUtils;
    }

    @Inject
    public void setCdnUtils(LargeObjectCdnUtils cdnUtils) {
        this.cdnUtils = cdnUtils;
    }
}
