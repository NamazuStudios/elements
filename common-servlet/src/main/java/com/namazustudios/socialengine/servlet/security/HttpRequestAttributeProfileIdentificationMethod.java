package com.namazustudios.socialengine.servlet.security;

import com.namazustudios.socialengine.exception.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class HttpRequestAttributeProfileIdentificationMethod implements ProfileIdentificationMethod {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestAttributeProfileIdentificationMethod.class);

    private HttpServletRequest httpServletRequest;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {

        final Object profile = getHttpServletRequest().getAttribute(Profile.PROFILE_ATTRIBUTE);

        if (profile == null) {
            throw new UnidentifiedProfileException();
        } else if (!(profile instanceof Profile)) {
            logger.error("{} is not instance of {}", profile, Profile.class.getName());
            throw new UnidentifiedProfileException();
        }

        return (Profile) profile;

    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

}
