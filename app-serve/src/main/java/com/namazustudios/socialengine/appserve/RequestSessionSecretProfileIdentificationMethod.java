package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.security.SessionSecretHeader;
import com.namazustudios.socialengine.service.ProfileOverrideService;

import javax.inject.Inject;

public class RequestSessionSecretProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Request request;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {

        final String profileId = new SessionSecretHeader(getRequest().getHeader()::getHeader)
            .getOverrideProfileId()
            .orElseThrow(() -> new UnidentifiedProfileException());

        return getProfileOverrideService()
            .findOverrideProfile(profileId)
            .orElseThrow(() -> new UnidentifiedProfileException());

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

//    @Inject
    public void setProfileOverrideService(ProfileOverrideService profileOverrideService) {
        this.profileOverrideService = profileOverrideService;
    }

}
