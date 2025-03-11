package dev.getelements.elements.service.profile;


import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.sdk.model.profile.UpdateProfileRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.service.profile.ProfileService;
import dev.getelements.elements.sdk.service.user.UserService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class UserProfileService implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    private User user;

    private UserService userService;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private ProfileServiceUtils profileServiceUtils;

    private ProfileImageObjectUtils profileImageObjectUtils;

    private Supplier<Profile> currentProfileSupplier;

    private Optional<Profile> currentProfileOptional;

    private LargeObjectDao largeObjectDao;

    private ElementRegistry elementRegistry;

    @Override
    public Pagination<Profile> getProfiles(final int offset, final int count,
                                           final String applicationNameOrId, final String userId,
                                           final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        Pagination<Profile> profiles = new Pagination<>();
        if (getUserService().isCurrentUserAlias(userId)) {
            profiles = getProfileDao()
                .getActiveProfiles(
                        offset, count,
                        applicationNameOrId, getUserService().getCurrentUser().getId(),
                        lowerBoundTimestamp, upperBoundTimestamp)
                .transform(this::redactPrivateInformation);
        } else if (userId == null || getUserService().isCurrentUser(userId)) {
            profiles = getProfileDao()
                .getActiveProfiles(
                    offset, count,
                    applicationNameOrId, userId,
                    lowerBoundTimestamp, upperBoundTimestamp)
                .transform(this::redactPrivateInformation);
        }
        return profileServiceUtils.profilesPageCdnSetup(profiles);
    }

    @Override
    public Pagination<Profile> getProfiles(
            int offset,
            int count,
            String search) {
        Pagination<Profile> profiles = getProfileDao()
                .getActiveProfiles(offset, count, search)
                .transform(this::redactPrivateInformation);
        return profileServiceUtils.profilesPageCdnSetup(profiles);
    }

    @Override
    public Profile getProfile(String profileId) {
        Profile profile = getProfileDao().getActiveProfile(profileId);
        return profileWithImageUrl(profile);
    }

    @Override
    public Profile getCurrentProfile() {
        return profileWithImageUrl(getCurrentProfileSupplier().get());
    }

    @Override
    public Optional<Profile> findCurrentProfile() {
        return getCurrentProfileOptional().map(profileServiceUtils::assignCdnUrl);
    }

    @Override
    public Profile updateProfile(final String profileId, final UpdateProfileRequest profileRequest) {

        checkUserAndProfile(getProfileDao().getActiveProfile(profileId).getUser().getId());
        profileRequest.setMetadata(null);

        final var profile = getProfileServiceUtils().getProfileForUpdate(profileId, profileRequest);
        return profileWithImageUrl(getProfileDao().updateActiveProfile(profile));

    }

    @Override
    public Profile createProfile(final CreateProfileRequest profileRequest) {

        checkUserAndProfile(profileRequest.getUserId());
        profileRequest.setMetadata(null);

        final Profile createdProfile = createNewProfile(profileRequest);
        LargeObject imageObject = profileImageObjectUtils.createImageObject(createdProfile);
        LargeObject persistedObject = largeObjectDao.createLargeObject(imageObject);

        LargeObjectReference referenceForPersistedObject = profileImageObjectUtils.createReference(persistedObject);
        createdProfile.setImageObject(referenceForPersistedObject);
        final Profile createdAndImageUpdatedProfile = getProfileDao().updateActiveProfile(createdProfile);

        final var event = Event.builder()
                .named(PROFILE_CREATED_EVENT)
                .argument(createdProfile)
                .build();

        getElementRegistry().publish(event);

        return profileWithImageUrl(createdAndImageUpdatedProfile);

    }

    private void checkUserAndProfile(final String id) {
        if (!Objects.equals(getUser().getId(), id)) {
            throw new InvalidDataException("Profile userId must match current userId.");
        }
    }

    private Profile createNewProfile(final CreateProfileRequest profileRequest) {
        final var profile = getProfileServiceUtils().getProfileForCreate(profileRequest);
        profileRequest.setMetadata(null);
        return getProfileDao().createOrReactivateProfile(profile);
    }

    @Override
    public void deleteProfile(String profileId) {

        if (!Objects.equals(getCurrentProfile().getId(), profileId)) {
            throw new NotFoundException();
        }

        getProfileDao().softDeleteProfile(profileId);
    }

    @Override
    public Profile updateProfileImage(final String profileId, final UpdateProfileImageRequest updateProfileImageRequest) throws IOException {
        final var profile = getCurrentProfile();

        checkUserAndProfile(getProfileDao().getActiveProfile(profile.getId()).getUser().getId());

        if (isNull(profile.getImageObject())) {
            logger.warn("Requested update profile which does not have large object assigned yet. Creating new LargeObject");
            LargeObject imageObject = profileImageObjectUtils.createImageObject(profile);
            imageObject.setMimeType(updateProfileImageRequest.getMimeType());
            LargeObject persistedObject = largeObjectDao.createLargeObject(imageObject);

            LargeObjectReference referenceForPersistedObject = profileImageObjectUtils.createReference(persistedObject);
            profile.setImageObject(referenceForPersistedObject);
        } else {
            LargeObject objectToUpdate = largeObjectDao.getLargeObject(profile.getImageObject().getId());
            LargeObject updatedObject = profileImageObjectUtils.updateProfileImageObject(profile, objectToUpdate, updateProfileImageRequest);
            largeObjectDao.updateLargeObject(updatedObject);
        }

        return profileWithImageUrl(getProfileDao().updateActiveProfile(profile));
    }

    private Profile profileWithImageUrl(Profile profile) {
        return profileServiceUtils.assignCdnUrl(profile);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public Optional<Profile> getCurrentProfileOptional() {
        return currentProfileOptional;
    }

    @Inject
    public void setCurrentProfileOptional(Optional<Profile> currentProfileOptional) {
        this.currentProfileOptional = currentProfileOptional;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    public ProfileServiceUtils getProfileServiceUtils() {
        return profileServiceUtils;
    }

    @Inject
    public void setProfileServiceUtils(ProfileServiceUtils profileServiceUtils) {
        this.profileServiceUtils = profileServiceUtils;
    }

    public ProfileImageObjectUtils getProfileImageObjectUtils() {
        return profileImageObjectUtils;
    }

    @Inject
    public void setProfileImageObjectUtils(ProfileImageObjectUtils profileImageObjectUtils) {
        this.profileImageObjectUtils = profileImageObjectUtils;
    }

    public LargeObjectDao getLargeObjectDao() {
        return largeObjectDao;
    }

    @Inject
    public void setLargeObjectDao(LargeObjectDao largeObjectDao) {
        this.largeObjectDao = largeObjectDao;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

}
