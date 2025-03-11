package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;

import dev.getelements.elements.sdk.service.profile.ProfileOverrideService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import static dev.getelements.elements.sdk.model.Headers.PROFILE_ID;

public class HttpRequestHeaderProfileIdentificationMethod implements ProfileIdentificationMethod {

    private HttpServletRequest httpServletRequest;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Optional<Profile> attempt() {
        final var profileId = getHttpServletRequest().getHeader(PROFILE_ID);
        return Optional.ofNullable(profileId).flatMap(getProfileOverrideService()::findOverrideProfile);
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
