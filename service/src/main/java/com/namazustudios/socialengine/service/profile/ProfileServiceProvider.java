package com.namazustudios.socialengine.service.profile;

import com.namazustudios.socialengine.service.ProfileService;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 6/27/17.
 */
public class ProfileServiceProvider implements Provider<ProfileService> {

    @Override
    public ProfileService get() {
        return Services.forbidden(ProfileService.class);
    }

}
