package dev.getelements.elements.servlet.security;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.security.ProfileIdentificationMethod;
import dev.getelements.elements.security.SessionSecretHeader;
import dev.getelements.elements.service.ProfileOverrideService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class HttpRequestSessionSecretProfileIdentificationMethod implements ProfileIdentificationMethod {

    private HttpServletRequest httpServletRequest;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Optional<Profile> attempt() {
        return SessionSecretHeader
                .withValueSupplier(getHttpServletRequest()::getHeader)
                .getOverrideProfileId()
                .flatMap(getProfileOverrideService()::findOverrideProfile);
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
