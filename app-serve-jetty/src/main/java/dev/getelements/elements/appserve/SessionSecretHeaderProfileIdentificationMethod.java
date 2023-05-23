package dev.getelements.elements.appserve;

import dev.getelements.elements.exception.profile.UnidentifiedProfileException;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.RequestHeader;
import dev.getelements.elements.security.ProfileIdentificationMethod;
import dev.getelements.elements.security.SessionSecretHeader;
import dev.getelements.elements.service.ProfileOverrideService;

import javax.inject.Inject;

public class SessionSecretHeaderProfileIdentificationMethod implements ProfileIdentificationMethod {

    private SessionSecretHeader sessionSecretHeader;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {

       final String profileId = getSessionSecretHeader()
            .getOverrideProfileId()
            .orElseThrow(UnidentifiedProfileException::new);

        return getProfileOverrideService()
            .findOverrideProfile(profileId)
            .orElseThrow(UnidentifiedProfileException::new);

    }

    public SessionSecretHeader getSessionSecretHeader() {
        return sessionSecretHeader;
    }

    @Inject
    public void setSessionSecretHeader(SessionSecretHeader sessionSecretHeader) {
        this.sessionSecretHeader = sessionSecretHeader;
    }

    public ProfileOverrideService getProfileOverrideService() {
        return profileOverrideService;
    }

    @Inject
    public void setProfileOverrideService(ProfileOverrideService profileOverrideService) {
        this.profileOverrideService = profileOverrideService;
    }

}
