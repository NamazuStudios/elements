package com.namazustudios.socialengine.servlet.security;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.service.ProfileOverrideService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static com.namazustudios.socialengine.Headers.PROFILE_ID;

public class HttpRequestHeaderProfileIdentificationMethod implements ProfileIdentificationMethod {

    private HttpServletRequest httpServletRequest;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {

        final String profileId = getHttpServletRequest().getHeader(PROFILE_ID);
        if (profileId == null) throw new UnidentifiedProfileException();

        return getProfileOverrideService()
            .findOverrideProfile(profileId)
            .orElseThrow(() -> new UnidentifiedProfileException());

    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public ProfileOverrideService getProfileOverrideService() {
        return profileOverrideService;
    }

    @Inject
    public void setProfileOverrideService(ProfileOverrideService profileOverrideService) {
        this.profileOverrideService = profileOverrideService;
    }

}
