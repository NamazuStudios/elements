package com.namazustudios.socialengine.servlet.security;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.security.SessionSecretHeader;
import com.namazustudios.socialengine.service.ProfileOverrideService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class HttpRequestSessionSecretProfileIdentificationMethod implements ProfileIdentificationMethod {

    private HttpServletRequest httpServletRequest;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {

        final String profileId = new SessionSecretHeader(getHttpServletRequest()::getHeader).getOverrideProfileId();
        if (profileId == null) throw new UnidentifiedProfileException();

        final Profile profile = getProfileOverrideService().findOverrideProfile(profileId);
        if (profile == null) throw new UnidentifiedProfileException();

        return profile;

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
