package dev.getelements.elements.service.user;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.profile.CreateProfileSignupRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.user.UserService;
import dev.getelements.elements.service.profile.SuperUserProfileService;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 3/26/15.
 */
public abstract class AbstractUserService implements UserService {

    private User currentUser;

    private ProfileDao profileDao;

    private NameService nameService;

    private ApplicationDao applicationDao;

    private SuperUserProfileService superUserProfileService;

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Inject
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public SuperUserProfileService getSuperUserProfileService() {
        return superUserProfileService;
    }

    @Inject
    public void setSuperUserProfileService(SuperUserProfileService superUserProfileService) {
        this.superUserProfileService = superUserProfileService;
    }

    /**
     * Creates a profile from the supplied user id and {@link CreateProfileSignupRequest}.
     *
     * @param userId the user id as specified by {@link User#getId()}
     * @param createProfileSignupRequest the {@link CreateProfileSignupRequest}
     * @return the created {@link Profile}
     */
    protected Profile createProfile(final String userId, final CreateProfileSignupRequest createProfileSignupRequest) {
        final var profile = new CreateProfileRequest();
        profile.setUserId(userId);
        profile.setImageUrl(createProfileSignupRequest.getImageUrl());
        profile.setDisplayName(createProfileSignupRequest.getDisplayName());
        profile.setApplicationId(createProfileSignupRequest.getApplicationId());
        return getSuperUserProfileService().createProfile(profile);
    }

    /**
     * Creates several {@link Profile}s. The created {@link Profile}s will be created using the.
     *
     * @param userId the {@link User} to assocaite with the new profile.
     * @param createProfileSignupRequests a {@link List} instance
     * @return
     */
    protected List<Profile> createProfiles(final String userId,
                                           final List<CreateProfileSignupRequest> createProfileSignupRequests) {
        return createProfileSignupRequests
                .stream()
                .map(req -> createProfile(userId, req))
                .collect(Collectors.toList());
    }

}
