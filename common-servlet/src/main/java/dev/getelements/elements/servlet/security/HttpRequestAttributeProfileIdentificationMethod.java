package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public class HttpRequestAttributeProfileIdentificationMethod implements ProfileIdentificationMethod {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestAttributeProfileIdentificationMethod.class);

    private HttpServletRequest httpServletRequest;

    @Override
    public Optional<Profile> attempt() {

        final Object profile = getHttpServletRequest().getAttribute(Profile.PROFILE_ATTRIBUTE);

        if (profile == null) {
            return Optional.empty();
        } else if (!(profile instanceof Profile)) {
            logger.error("{} is not instance of {}", profile, Profile.class.getName());
            return Optional.empty();
        } else {
            return Optional.of(profile).map(Profile.class::cast);
        }

    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    @Inject
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

}
