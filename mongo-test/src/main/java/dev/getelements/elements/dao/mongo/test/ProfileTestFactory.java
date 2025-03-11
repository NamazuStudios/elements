package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;

import jakarta.inject.Inject;

import static java.lang.String.format;

public class ProfileTestFactory {

    private ProfileDao profileDao;

    public Profile makeMockProfile(final User user, final Application application) {
        final var profile =  new Profile();
        profile.setUser(user);
        profile.setApplication(application);
        profile.setDisplayName(format("display-name-%s", user.getName()));
        profile.setImageUrl(format("http://example.com/%s.png", user.getName()));
        return getProfileDao().createOrReactivateProfile(profile);
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

}
