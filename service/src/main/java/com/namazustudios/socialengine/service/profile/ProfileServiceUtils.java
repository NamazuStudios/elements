package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.application.ApplicationNotFoundException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.profile.UpdateProfileRequest;
import com.namazustudios.socialengine.service.NameService;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ProfileServiceUtils {

    private UserDao userDao;

    private NameService nameService;

    private ApplicationDao applicationDao;

    public Profile getProfileForCreate(final CreateProfileRequest profileRequest) {

        final var profile = new Profile();

        try {
            profile.setUser(getUserDao().getActiveUser(profileRequest.getUserId()));
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

}
