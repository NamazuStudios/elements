package dev.getelements.elements.service.follower;

import dev.getelements.elements.dao.FollowerDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.follower.CreateFollowerRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.FollowerService;

import javax.inject.Inject;
import java.util.Objects;

public class UserFollowerService implements FollowerService {

    private User user;

    private FollowerDao followerDao;

    private ProfileDao profileDao;

    @Override
    public Pagination<Profile> getFollowers(final String profileId, final int offset, final int count) {
        return getFollowerDao().getFollowersForProfile(profileId, offset, count).transform(this::redactPrivateInformation);
    }

    @Override
    public Profile getFollower(final String profileId, final String followedId) {
        return this.redactPrivateInformation(getFollowerDao().getFollowerForProfile(profileId, followedId));
    }

    @Override
    public void createFollower(final String profileId, final CreateFollowerRequest createFollowerRequest) {
        checkUserAndProfile(profileId);
        getFollowerDao().createFollowerForProfile(profileId, createFollowerRequest);
    }

    @Override
    public void deleteFollower(final String profileId, final String profileToUnfollowId) {
        checkUserAndProfile(profileId);
        getFollowerDao().deleteFollowerForProfile(profileId, profileToUnfollowId);
    }

    private void checkUserAndProfile(final String profileId) {
        final Profile userProfile = getProfileDao().getActiveProfile(profileId);
        if (!Objects.equals(getUser(), userProfile.getUser())) {
            throw new InvalidDataException("Profile user must match current user.");
        }

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public FollowerDao getFollowerDao() {
        return followerDao;
    }

    @Inject
    public void setFollowerDao(FollowerDao followerDao) {
        this.followerDao = followerDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

}
