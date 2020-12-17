package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.profile.UpdateProfileRequest;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.EventContext;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.service.ProfileService;

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

    private ProfileDao profileDao;

    private ContextFactory contextFactory;

    private Supplier<Profile> currentProfileSupplier;

    private Provider<Attributes> attributesProvider;

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
    public Pagination<Profile> getProfiles(
            int offset,
            int count,
            String search
    ) {
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
    public Profile updateProfile(UpdateProfileRequest profileRequest) {
        return getProfileDao().updateActiveProfile(profileRequest);
    }

    @Override
    public Profile createProfile(CreateProfileRequest profileRequest) {
        final EventContext eventContext = getContextFactory().getContextForApplication(profileRequest.getApplicationId()).getEventContext();
        final Profile createdProfile = getProfileDao().createOrReactivateProfile(profileRequest);
        final Attributes attributes = new SimpleAttributes.Builder()
                .from(getAttributesProvider().get(), (n, v) -> v instanceof Serializable)
                .build();
        eventContext.postAsync(PROFILE_CREATED_EVENT, attributes, createdProfile);
        return createdProfile;
    }

    @Override
    public void deleteProfile(String profileId) {
        getProfileDao().softDeleteProfile(profileId);
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public ContextFactory getContextFactory() {
        return contextFactory;
    }

    @Inject
    public void setContextFactory(ContextFactory contextFactory) {
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

}
