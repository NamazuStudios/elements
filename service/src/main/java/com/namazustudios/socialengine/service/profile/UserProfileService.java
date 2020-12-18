package com.namazustudios.socialengine.service.profile;


import com.namazustudios.socialengine.dao.ContextFactory;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.UpdateProfileRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.EventContext;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.service.ProfileService;
import com.namazustudios.socialengine.service.UserService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class UserProfileService implements ProfileService {

    private User user;

    private UserService userService;

    private ProfileDao profileDao;

    private ContextFactory contextFactory;

    private Supplier<Profile> currentProfileSupplier;

    private Provider<Attributes> attributesProvider;

    public static final String PROFILE_CREATED_EVENT = "com.namazustudios.elements.service.profile.created";

    @Override
    public Pagination<Profile> getProfiles(final int offset, final int count,
                                           final String applicationNameOrId, final String userId,
                                           final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        if (getUserService().isCurrentUserAlias(userId)) {
            return getProfileDao()
                .getActiveProfiles(
                        offset, count,
                        applicationNameOrId, getUserService().getCurrentUser().getId(),
                        lowerBoundTimestamp, upperBoundTimestamp)
                .transform(this::redactPrivateInformation);
        } else if (userId == null || getUserService().isCurrentUser(userId)) {
            return getProfileDao()
                .getActiveProfiles(
                    offset, count,
                    applicationNameOrId, userId,
                    lowerBoundTimestamp, upperBoundTimestamp)
                .transform(this::redactPrivateInformation);
        } else {
            return new Pagination<>();
        }
    }

    @Override
    public Pagination<Profile> getProfiles(
            int offset,
            int count,
            String search) {
        return getProfileDao()
            .getActiveProfiles(offset, count, search)
            .transform(this::redactPrivateInformation);
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
        checkUserAgainstProfileId(profileId);
        return getProfileDao().updateActiveProfile(profileId, profileRequest);
    }

    @Override
    public Profile createProfile(CreateProfileRequest profileRequest) {
        checkUserAgainstUserId(profileRequest.getUserId());
        final EventContext eventContext = getContextFactory().getContextForApplication(profileRequest.getApplicationId()).getEventContext();
        final Profile createdProfile = getProfileDao().createOrReactivateProfile(profileRequest);
        final Attributes attributes = new SimpleAttributes.Builder()
                .from(getAttributesProvider().get(), (n, v) -> v instanceof Serializable)
                .build();
        eventContext.postAsync(PROFILE_CREATED_EVENT, attributes, createdProfile);
        return createdProfile;
    }

    private void checkUserAgainstUserId(final String userId) {
        if (!Objects.equals(getUser().getId(), userId)) {
            throw new InvalidDataException("Profile userId must match current userId.");
        }
    }

    private void checkUserAgainstProfileId(final String profileId) {
        if (!Objects.equals(getUser().getId(), !Objects.equals(getUser().getId(), getProfileDao().getUserIdForProfile(profileId)))) {
            throw new InvalidDataException("Profile userId must match current userId.");
        }
    }

    @Override
    public void deleteProfile(String profileId) {

        if (!Objects.equals(getCurrentProfile().getId(), profileId)) {
            throw new NotFoundException();
        }

        getProfileDao().softDeleteProfile(profileId);

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
