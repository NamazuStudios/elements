package dev.getelements.elements.service.profile;

import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.profile.CreateProfileRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.model.profile.UpdateProfileRequest;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.exception.NodeNotFoundException;
import dev.getelements.elements.service.NameService;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.Unscoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Supplier;

import static dev.getelements.elements.service.profile.UserProfileService.PROFILE_CREATED_EVENT;
import static dev.getelements.elements.service.profile.UserProfileService.PROFILE_CREATED_EVENT_LEGACY;
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

    private Context.Factory contextFactory;

    private NameService nameService;

    private Optional<Profile> currentProfileOptional;

    private Supplier<Profile> currentProfileSupplier;

    private Provider<Attributes> attributesProvider;

    private ProfileServiceUtils profileServiceUtils;

    private ProfileImageObjectUtils profileImageObjectUtils;

    private LargeObjectDao largeObjectDao;

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
        return profileServiceUtils.assignCdnUrl(profile);
    }

    @Override
    public Profile getCurrentProfile() {
        return profileServiceUtils.assignCdnUrl(getCurrentProfileSupplier().get());
    }

    @Override
    public Optional<Profile> findCurrentProfile() {
        return getCurrentProfileOptional().map(profileServiceUtils::assignCdnUrl);
    }

    @Override
    public Profile updateProfile(String profileId, UpdateProfileRequest profileRequest) {
        final var profile = getProfileServiceUtils().getProfileForUpdate(profileId, profileRequest);
        return getProfileDao().updateActiveProfile(profile);
    }

    @Override
    public Profile createProfile(final CreateProfileRequest createProfileRequest) {
        final var createdProfile = createNewProfile(createProfileRequest);

        final var eventContext = getContextFactory()
            .getContextForApplication(createdProfile.getApplication().getId())
            .getEventContext();

        final var attributes = new SimpleAttributes.Builder()
            .from(getAttributesProvider().get(), (n, v) -> v instanceof Serializable)
            .build();

        LargeObject imageObject = profileImageObjectUtils.createImageObject(createdProfile);

        LargeObject persistedObject = largeObjectDao.createLargeObject(imageObject);
        LargeObjectReference referenceForPersistedObject = profileImageObjectUtils.createReference(persistedObject);
        createdProfile.setImageObject(referenceForPersistedObject);
        getProfileDao().updateActiveProfile(createdProfile);

        try {
            eventContext.postAsync(PROFILE_CREATED_EVENT, attributes, createdProfile);
        } catch (NodeNotFoundException ex) {
            logger.warn("Unable to dispatch the {} event handler.", PROFILE_CREATED_EVENT, ex);
        }

        try {
            eventContext.postAsync(PROFILE_CREATED_EVENT_LEGACY, attributes, createdProfile);
        } catch (NodeNotFoundException ex) {
            logger.warn("Unable to dispatch the {} event handler.", PROFILE_CREATED_EVENT, ex);
        }

        return createdProfile;
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
            throw new NotFoundException("LargeObject for image was not yet assigned to this profile.");
        }

        LargeObject objectToUpdate = largeObjectDao.getLargeObject(profile.getImageObject().getId());
        LargeObject updatedObject = profileImageObjectUtils.updateProfileImageObject(profile, objectToUpdate, updateProfileImageRequest);
        largeObjectDao.updateLargeObject(updatedObject);

        profileImageObjectUtils.updateProfileReference(profile.getImageObject(), updatedObject);

        return getProfileDao().updateActiveProfile(profile);
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

    public Context.Factory getContextFactory() {
        return contextFactory;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void setNameService(@Unscoped NameService nameService) {
        this.nameService = nameService;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Inject
    public void setContextFactory(Context.Factory contextFactory) {
        this.contextFactory = contextFactory;
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

    public Provider<Attributes> getAttributesProvider() {
        return attributesProvider;
    }

    @Inject
    public void setAttributesProvider(Provider<Attributes> attributesProvider) {
        this.attributesProvider = attributesProvider;
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
}
