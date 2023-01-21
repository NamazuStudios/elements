package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;

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
