package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.service.ProfileOverrideService;

import javax.inject.Inject;

import static com.namazustudios.socialengine.Headers.PROFILE_ID;

public class RequestHeaderProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Request request;

    private ProfileOverrideService profileOverrideService;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {

        final String profileId = getRequest()
            .getHeader()
            .getHeader(PROFILE_ID)
            .map(o -> o.toString())
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

    @Inject
    public void setProfileOverrideService(final ProfileOverrideService profileOverrideService) {
        this.profileOverrideService = profileOverrideService;
    }

}
