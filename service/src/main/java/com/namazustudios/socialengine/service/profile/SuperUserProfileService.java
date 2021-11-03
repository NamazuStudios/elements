package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.profile.UpdateProfileRequest;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.service.NameService;
import com.namazustudios.socialengine.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.service.profile.UserProfileService.PROFILE_CREATED_EVENT;

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

    private Supplier<Profile> currentProfileSupplier;

    private Provider<Attributes> attributesProvider;

    private ProfileServiceUtils profileServiceUtils;

    @Override
    public Pagination<Profile> getProfiles(final int offset, final int count,
                                           final String applicationNameOrId, final String userId,
                                           final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        return getProfileDao().getActiveProfiles(
                offset, count,
                applicationNameOrId, userId,
                lowerBoundTimestamp, upperBoundTimestamp);
    }

    @Override
    public Pagination<Profile> getProfiles(int offset, int count, String search) {
        return getProfileDao().getActiveProfiles(offset, count, search);
    }

    @Override
    public Profile getProfile(String profileId) {
        return getProfileDao().getActiveProfile(profileId);
    }

    @Override
    public Profile getCurrentProfile() {
        return getCurrentProfileSupplier().get();
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

        try {
            eventContext.postAsync(PROFILE_CREATED_EVENT, attributes, createdProfile);
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
    public void setNameService(NameService nameService) {
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

}
