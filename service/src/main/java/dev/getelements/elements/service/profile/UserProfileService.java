package dev.getelements.elements.service.profile;


import com.google.common.base.Strings;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.profile.CreateProfileRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.profile.UpdateProfileRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.EventContext;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.exception.NodeNotFoundException;
import dev.getelements.elements.service.NameService;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

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

    private Context.Factory contextFactory;

    private Supplier<Profile> currentProfileSupplier;

    private Provider<Attributes> attributesProvider;

    public static final String PROFILE_CREATED_EVENT = "dev.getelements.elements.service.profile.created";

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

        try {
            eventContext.postAsync(PROFILE_CREATED_EVENT, attributes, createdProfile);
        } catch (NodeNotFoundException ex) {
            logger.warn("Unable to dispatch the {} event handler.", PROFILE_CREATED_EVENT, ex);
        }

        return createdProfile;
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
