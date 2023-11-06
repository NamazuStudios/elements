package dev.getelements.elements.service.profile;


import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.profile.CreateProfileRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.model.profile.UpdateProfileRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.EventContext;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.exception.NodeNotFoundException;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.Serializable;
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

    private Context.Factory contextFactory;

    private Supplier<Profile> currentProfileSupplier;

    private Optional<Profile> currentProfileOptional;

    private Provider<Attributes> attributesProvider;

    private LargeObjectDao largeObjectDao;

    public static final String PROFILE_CREATED_EVENT = "dev.getelements.elements.service.profile.created";

    public static final String PROFILE_CREATED_EVENT_LEGACY = "com.namazustudios.elements.service.profile.created";

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
    public Profile updateProfile(final String profileId, final UpdateProfileRequest profileRequest) {

        checkUserAndProfile(getProfileDao().getActiveProfile(profileId).getUser().getId());
        profileRequest.setMetadata(null);

        final var profile = getProfileServiceUtils().getProfileForUpdate(profileId, profileRequest);
        return getProfileDao().updateActiveProfile(profile);

    }

    @Override
    public Profile createProfile(final CreateProfileRequest profileRequest) {

        checkUserAndProfile(profileRequest.getUserId());
        profileRequest.setMetadata(null);

        final EventContext eventContext = getContextFactory()
            .getContextForApplication(profileRequest.getApplicationId())
            .getEventContext();

        final Profile createdProfile = createNewProfile(profileRequest);
        final Attributes attributes = new SimpleAttributes.Builder()
            .from(getAttributesProvider().get(), (n, v) -> v instanceof Serializable)
            .build();

        LargeObject imageObject = profileImageObjectUtils.createImageObject(createdProfile);
        LargeObject persistedObject = largeObjectDao.createLargeObject(imageObject);

        LargeObjectReference referenceForPersistedObject = profileImageObjectUtils.createReference(persistedObject);
        createdProfile.setImageObject(referenceForPersistedObject);
        Profile finalCreatedProfile = getProfileDao().updateActiveProfile(createdProfile);

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

        return finalCreatedProfile;
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
        checkUserAndProfile(profile.getId());

        if (isNull(profile.getImageObject())) {
            logger.warn("Requested update profile which does not have large object assigned yet. Creating new LargeObject");
            LargeObject imageObject = profileImageObjectUtils.createImageObject(profile);
            LargeObject persistedObject = largeObjectDao.createLargeObject(imageObject);

            LargeObjectReference referenceForPersistedObject = profileImageObjectUtils.createReference(persistedObject);
            profile.setImageObject(referenceForPersistedObject);
            getProfileDao().updateActiveProfile(profile);
            return profile;
        }

        LargeObject objectToUpdate = largeObjectDao.getLargeObject(profile.getImageObject().getId());
        LargeObject updatedObject = profileImageObjectUtils.updateProfileImageObject(profile, objectToUpdate, updateProfileImageRequest);
        largeObjectDao.updateLargeObject(updatedObject);

        profileImageObjectUtils.updateProfileReference(profile.getImageObject(), updatedObject);

        return getProfileDao().updateActiveProfile(profile);
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

    public Context.Factory getContextFactory() {
        return contextFactory;
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
