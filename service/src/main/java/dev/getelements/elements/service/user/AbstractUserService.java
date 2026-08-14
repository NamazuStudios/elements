package dev.getelements.elements.service.user;

import dev.getelements.elements.rt.exception.BadRequestException;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.profile.CreateProfileSignupRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserCreateRequest;
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

    private ElementRegistry elementRegistry;

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

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
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

    /**
     * Auto-creates the user's primary profile for the application named by
     * {@link UserCreateRequest#getAutoCreateProfileApplicationNameOrId()}, if requested. This is an explicit, opt-in
     * action: if that field is null, this is a no-op, preserving pre-existing behavior. If the named application
     * also appears in {@link UserCreateRequest#getProfiles()}, this throws a {@link BadRequestException}, since the
     * two mechanisms conflict for the same application. Otherwise, a profile is only actually created if the
     * application is configured for it (i.e. {@link dev.getelements.elements.sdk.model.application.Application#getAutoCreateProfile()}
     * is {@code true} and {@link dev.getelements.elements.sdk.model.application.Application#getMaxProfiles()} is at
     * least 1) -- if not, this silently no-ops rather than erroring, since the field names the feature to invoke,
     * but the application's own configuration still governs whether it does anything.
     *
     * @param userId the id of the newly created {@link User}
     * @param userCreateRequest the original {@link UserCreateRequest}
     * @return the auto-created {@link Profile}, or null if none was created
     */
    protected Profile autoCreateExplicitProfileIfRequested(final String userId,
                                                            final UserCreateRequest userCreateRequest) {

        final var applicationNameOrId = userCreateRequest.getAutoCreateProfileApplicationNameOrId();

        if (applicationNameOrId == null) {
            return null;
        }

        final var application = getApplicationDao().getApplication(applicationNameOrId);

        final var requestedProfiles = userCreateRequest.getProfiles();

        if (requestedProfiles != null) {
            final var conflicts = requestedProfiles.stream().anyMatch(requested ->
                    applicationNameOrId.equals(requested.getApplicationId())
                            || application.getId().equals(requested.getApplicationId()));

            if (conflicts) {
                throw new BadRequestException(
                        "Cannot both explicitly request a profile and request auto-create for application " +
                        applicationNameOrId);
            }
        }

        final var maxProfiles = application.getMaxProfiles();

        if (!Boolean.TRUE.equals(application.getAutoCreateProfile()) || maxProfiles == null || maxProfiles < 1) {
            return null;
        }

        final var signupRequest = new CreateProfileSignupRequest();
        signupRequest.setApplicationId(application.getId());

        return createProfile(userId, signupRequest);

    }

}
