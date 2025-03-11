package dev.getelements.elements.service.profile;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.sdk.model.profile.UpdateProfileRequest;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.sdk.service.profile.ProfileService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static java.util.Objects.isNull;

/**
 * Provides full access to the {@link Profile} and related types.  Should be
 * used in conjunction with users SUPERUSER level access.
 *
 * Created by patricktwohig on 6/28/17.
 */
public class SuperUserProfileService implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(SuperUserProfileService.class);

    private UserDao userDao;

    private ProfileDao profileDao;

    private ApplicationDao applicationDao;

    private NameService nameService;

    private Optional<Profile> currentProfileOptional;

    private Supplier<Profile> currentProfileSupplier;

    private ProfileServiceUtils profileServiceUtils;

    private ProfileImageObjectUtils profileImageObjectUtils;

    private LargeObjectDao largeObjectDao;

    private ElementRegistry elementRegistry;

    @Override
    public Pagination<Profile> getProfiles(final int offset, final int count,
                                           final String applicationNameOrId, final String userId,
                                           final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        Pagination<Profile> profiles = getProfileDao().getActiveProfiles(
                offset, count,
                applicationNameOrId, userId,
                lowerBoundTimestamp, upperBoundTimestamp);
        return profileServiceUtils.profilesPageCdnSetup(profiles);
    }

    @Override
    public Pagination<Profile> getProfiles(int offset, int count, String search) {
        Pagination<Profile> profiles = getProfileDao().getActiveProfiles(offset, count, search);
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
    public Profile updateProfile(String profileId, UpdateProfileRequest profileRequest) {
        final var profile = getProfileServiceUtils().getProfileForUpdate(profileId, profileRequest);
        return profileWithImageUrl(getProfileDao().updateActiveProfile(profile));
    }

    @Override
    public Profile createProfile(final CreateProfileRequest createProfileRequest) {

        final var createdProfile = createNewProfile(createProfileRequest);
        final var imageObject = getProfileImageObjectUtils().createImageObject(createdProfile);

        LargeObject persistedObject = getLargeObjectDao().createLargeObject(imageObject);
        LargeObjectReference referenceForPersistedObject = getProfileImageObjectUtils().createReference(persistedObject);
        createdProfile.setImageObject(referenceForPersistedObject);
        final Profile createdAndImageUpdatedProfile = getProfileDao().updateActiveProfile(createdProfile);

        final var event = Event.builder()
                .named(PROFILE_CREATED_EVENT)
                .argument(createdProfile)
                .build();

        getElementRegistry().publish(event);

        return profileWithImageUrl(createdAndImageUpdatedProfile);
    }

    private Profile createNewProfile(final CreateProfileRequest profileRequest) {
        final var profile = getProfileServiceUtils().getProfileForCreate(profileRequest);
        return getProfileDao().createOrReactivateProfile(profile);
    }

    @Override
    public void deleteProfile(String profileId) {
        getProfileDao().softDeleteProfile(profileId);
    }

    @Override
    public Profile updateProfileImage(final String profileId, final UpdateProfileImageRequest updateProfileImageRequest) throws IOException {
        final var profile = getProfileDao().getActiveProfile(profileId);

        if (isNull(profile.getImageObject())) {
            logger.warn("Requested update profile which does not have large object assigned yet. Creating new LargeObject");
            LargeObject imageObject = profileImageObjectUtils.createImageObject(profile);
            LargeObject persistedObject = largeObjectDao.createLargeObject(imageObject);

            LargeObjectReference referenceForPersistedObject = profileImageObjectUtils.createReference(persistedObject);
            profile.setImageObject(referenceForPersistedObject);
        } else {
            LargeObject objectToUpdate = largeObjectDao.getLargeObject(profile.getImageObject().getId());
            LargeObject updatedObject = profileImageObjectUtils.updateProfileImageObject(profile, objectToUpdate, updateProfileImageRequest);
            LargeObject persistedObject = largeObjectDao.updateLargeObject(updatedObject);

            LargeObjectReference referenceForPersistedObject = profileImageObjectUtils.createReference(persistedObject);
            profile.setImageObject(referenceForPersistedObject);
        }

        return profileWithImageUrl(getProfileDao().updateActiveProfile(profile));
    }

    private Profile profileWithImageUrl(Profile profile) {
        return profileServiceUtils.assignCdnUrl(profile);
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(@Named(UNSCOPED) NameService nameService) {
        this.nameService = nameService;
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
