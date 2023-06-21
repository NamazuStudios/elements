package dev.getelements.elements.servlet.security;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.security.ProfileIdentificationMethod;
import dev.getelements.elements.service.ProfileOverrideService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static dev.getelements.elements.Headers.PROFILE_ID;

public class HttpRequestHeaderProfileIdentificationMethod implements ProfileIdentificationMethod {

    private HttpServletRequest httpServletRequest;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {

        final String profileId = getHttpServletRequest().getHeader(PROFILE_ID);
        if (profileId == null) throw new UnidentifiedProfileException();

        return getProfileOverrideService()
            .findOverrideProfile(profileId)
            .orElseThrow(UnidentifiedProfileException::new);

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
