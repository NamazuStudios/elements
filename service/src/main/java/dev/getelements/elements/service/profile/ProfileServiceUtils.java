package dev.getelements.elements.service.profile;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.application.ApplicationNotFoundException;
import dev.getelements.elements.sdk.model.exception.user.UserNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.profile.UpdateProfileRequest;
import dev.getelements.elements.sdk.service.name.NameService;
import dev.getelements.elements.service.largeobject.LargeObjectCdnUtils;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import static com.google.common.base.Strings.isNullOrEmpty;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static java.util.Objects.nonNull;

public class ProfileServiceUtils {

    private UserDao userDao;

    private NameService nameService;

    private ApplicationDao applicationDao;

    private LargeObjectCdnUtils largeObjectCdnUtils;

    public Profile getProfileForCreate(final CreateProfileRequest profileRequest) {

        final var profile = new Profile();

        try {
            profile.setUser(getUserDao().getUser(profileRequest.getUserId()));
            profile.setApplication(getApplicationDao().getActiveApplication(profileRequest.getApplicationId()));
        } catch (UserNotFoundException | ApplicationNotFoundException ex) {
            throw new InvalidDataException(ex);
        }

        profile.setImageUrl(profileRequest.getImageUrl());
        profile.setDisplayName(profileRequest.getDisplayName());
        profile.setMetadata(profileRequest.getMetadata());

        if (profile.getDisplayName() == null || profile.getDisplayName().trim().isEmpty())
            profile.setDisplayName(getNameService().generateQualifiedName());

        return profile;

    }

    public Profile getProfileForUpdate(final String profileId, final UpdateProfileRequest profileRequest) {

        final var updates = new Profile();
        updates.setId(profileId);

        if (!isNullOrEmpty(profileRequest.getDisplayName())) {
            updates.setDisplayName(profileRequest.getDisplayName());
        }

        if (!isNullOrEmpty(profileRequest.getImageUrl())) {
            updates.setImageUrl(profileRequest.getImageUrl());
        }

        final var metadata = profileRequest.getMetadata();
        updates.setMetadata(metadata);

        return updates;

    }

    Profile assignCdnUrl(Profile profile) {
        LargeObjectReference imageObject = profile.getImageObject();
        if (nonNull(imageObject)) {
            String imageUrl = largeObjectCdnUtils.assembleCdnUrl(imageObject.getId());
            imageObject.setUrl(imageUrl);
        }
        return profile;
    }

    Pagination<Profile> profilesPageCdnSetup(Pagination<Profile> profiles) {
        profiles.getObjects().forEach(this::assignCdnUrl);
        return profiles;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public NameService getNameService() {
        return nameService;
    }

    @Inject
    public void
    setNameService(@Named(UNSCOPED) NameService nameService) {
        this.nameService = nameService;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public LargeObjectCdnUtils getLargeObjectCdnUtils() {
        return largeObjectCdnUtils;
    }

    @Inject
    public void setLargeObjectCdnUtils(LargeObjectCdnUtils largeObjectCdnUtils) {
        this.largeObjectCdnUtils = largeObjectCdnUtils;
    }
}
