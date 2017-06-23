package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.service.FacebookAuthService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 6/23/17.
 */
public class FacebookAuthServiceProvider implements Provider<FacebookAuthService> {

    private Provider<AnonFacebookAuthService> anonFacebookAuthServiceProvider;

    @Override
    public FacebookAuthService get() {
        // TODO Assess other user levels as needed
        return getAnonFacebookAuthServiceProvider().get();
    }

    public Provider<AnonFacebookAuthService> getAnonFacebookAuthServiceProvider() {
        return anonFacebookAuthServiceProvider;
    }

    @Inject
    public void setAnonFacebookAuthServiceProvider(Provider<AnonFacebookAuthService> anonFacebookAuthServiceProvider) {
        this.anonFacebookAuthServiceProvider = anonFacebookAuthServiceProvider;
    }

}
