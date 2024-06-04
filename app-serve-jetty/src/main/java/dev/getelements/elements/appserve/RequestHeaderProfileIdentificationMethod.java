package dev.getelements.elements.appserve;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.Request;
import dev.getelements.elements.security.ProfileIdentificationMethod;
import dev.getelements.elements.service.ProfileOverrideService;

import javax.inject.Inject;

import java.util.Optional;

import static dev.getelements.elements.Headers.PROFILE_ID;

public class RequestHeaderProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Request request;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Optional<Profile> attempt() {
        return getRequest()
                .getHeader()
                .getHeader(PROFILE_ID)
                .map(Object::toString)
                .flatMap(getProfileOverrideService()::findOverrideProfile);
    }

    public Request getRequest() {
        return request;
    }

    @Inject
    public void setRequest(Request request) {
        this.request = request;
    }

    public ProfileOverrideService getProfileOverrideService() {
        return profileOverrideService;
    }

    @Inject
    public void setProfileOverrideService(ProfileOverrideService profileOverrideService) {
        this.profileOverrideService = profileOverrideService;
    }

}
