package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.security.SessionSecretHeader;
import com.namazustudios.socialengine.service.ProfileOverrideService;

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
